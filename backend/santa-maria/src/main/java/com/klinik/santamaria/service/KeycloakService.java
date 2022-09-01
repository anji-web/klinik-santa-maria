package com.klinik.santamaria.service;
import com.klinik.santamaria.helper.LoginDto;
import com.klinik.santamaria.helper.UsersDto;
import com.klinik.santamaria.model.Role;
import com.klinik.santamaria.model.Users;
import com.klinik.santamaria.repository.RolesDao;
import com.klinik.santamaria.repository.UsersDao;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KeycloakService {
  @Value("${keycloak.auth-server-url}")
  private String url;
  @Value("${keycloak.realm}")
  private String realm;
  @Value("${keycloak.resource}")
  private String clientId;
  @Value("${keycloak.credentials.secret}")
  private String clientSecret;
  @Value("${mtt.realm.master}")
  private String realmMaster;
  @Value("${mtt.realm.master.user}")
  private String realmMasterUser;
  @Value("${mtt.realm.master.password}")
  private String realmMasterPassword;
  @Value("${mtt.realm.master.client}")
  private String realmMasterClient;

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private UsersDao usersDao;

  @Autowired
  private RolesDao roleDao;
  private UsersResource getUserResources() {
    Keycloak keycloak = KeycloakBuilder.builder()
        .serverUrl(url)
        .realm(realmMaster)
        .username(realmMasterUser)
        .password(realmMasterPassword)
        .clientId(realmMasterClient)
        .resteasyClient(new ResteasyClientBuilder().build())
        .build();
    RealmResource realmResource = keycloak.realm(realm);
    return realmResource.users();
  }

  private RealmResource getRealmResources() {
    Keycloak keycloak = KeycloakBuilder.builder()
        .serverUrl(url)
        .realm(realmMaster)
        .username(realmMasterUser)
        .password(realmMasterPassword)
        .clientId(realmMasterClient)
        .resteasyClient(new ResteasyClientBuilder().build())
        .build();
    return keycloak.realm(realm);
  }

  public String getToken(LoginDto userLogin) throws IOException {

    try (Keycloak keycloak = connectKeycloak()) {
      String response = null;
      List<NameValuePair> urlParam = new ArrayList<>();
      urlParam.add(new BasicNameValuePair("grant_type", "password"));
      urlParam.add(new BasicNameValuePair("username", userLogin.getUsername()));
      urlParam.add(new BasicNameValuePair("password", userLogin.getPassword()));
      urlParam.add(new BasicNameValuePair("client_id", clientId));
      urlParam.add(new BasicNameValuePair("client_secret", clientSecret));
      response = sendPost(urlParam);
      if (response.toLowerCase().contains("error")) {
        return "username atau password salah";
      }
      return response;
    }
  }

  public String createUserToKeycloak(UsersDto users) throws Exception {
    Optional<Users> checkExistUser = usersDao.findByEmail(users.getEmail());
    if (!checkExistUser.isEmpty()){
      throw new Exception("User sudah terdaftar");
    }
    int status = 0;
    Keycloak keycloak = connectKeycloak();
    UsersResource usersResource = getUserResources();
    UserRepresentation userRepresentation = new UserRepresentation();
    String[] getFirstNameAndLastName = users.getFullName().split(" ");
    userRepresentation.setUsername(users.getUsername());
    userRepresentation.setEmail(users.getEmail());
    userRepresentation.setFirstName(getFirstNameAndLastName[0]);
    if (getFirstNameAndLastName.length > 2){
      userRepresentation.setLastName(getFirstNameAndLastName[1] + " " + getFirstNameAndLastName[2]);
    }else if (getFirstNameAndLastName.length == 2){
      userRepresentation.setLastName(getFirstNameAndLastName[1]);
    }
    else if (getFirstNameAndLastName.length < 2){
      userRepresentation.setLastName("");
    }
    userRepresentation.setEnabled(true);
    try(Response res = usersResource.create(userRepresentation)) {
      status = res.getStatus();
      if (status == 201){
        String uid = res.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(false);
        credential.setValue(users.getPassword());
        usersResource.get(uid).resetPassword(credential);
        RealmResource realmResource = getRealmResources();
        Role role = roleDao.getById(users.getRole());
        String userRole = role.getRoleName();
        ClientRepresentation clientRepresentation = realmResource.clients().findByClientId(clientId).get(0);
        RoleRepresentation roleRepresentation = realmResource
            .clients()
            .get(clientRepresentation.getId()).roles().get(userRole).toRepresentation();
        realmResource.users().get(uid).roles().clientLevel(clientRepresentation.getId()).add(
            Arrays.asList(roleRepresentation));
        keycloak.close();
        return uid;
      }else {
        return null;
      }
    }catch (Exception e){
      throw new Exception(e.getMessage());
    }
  }


  private String sendPost(List<NameValuePair> urlPost) throws IOException {
    StringBuilder buffer = new StringBuilder();
    HttpClient client = HttpClientBuilder.create().build();
    HttpPost post = new HttpPost(url + "/realms/" + realm + "/protocol/openid-connect/token");
    post.setEntity(new UrlEncodedFormEntity(urlPost));

    HttpResponse response = client.execute(post);

    try (BufferedReader result = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
      String line = "";
      while ((line = result.readLine()) != null) {
        buffer.append(line);
      }
    } catch (Exception e) {
      throw e;
    }
    return buffer.toString();
  }


  private Keycloak connectKeycloak() {
    Keycloak keycloak = KeycloakBuilder.builder()
        .serverUrl(url)
        .realm(realmMaster)
        .username(realmMasterUser)
        .password(realmMasterPassword)
        .clientId(realmMasterClient)
        .resteasyClient(new ResteasyClientBuilder().build())
        .build();
    return keycloak;
  }

  public boolean deleteUser(String userId){
    if (userId.isEmpty())
        return false;
    Keycloak keycloak = connectKeycloak();
    UsersResource usersResource = getUserResources();
    usersResource.delete(userId);
    keycloak.close();
    return true;
  }

}

