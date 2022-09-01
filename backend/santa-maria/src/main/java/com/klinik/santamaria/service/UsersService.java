package com.klinik.santamaria.service;


import com.klinik.santamaria.helper.LoginDto;
import com.klinik.santamaria.helper.UsersDto;
import com.klinik.santamaria.model.Role;
import com.klinik.santamaria.model.Users;
import com.klinik.santamaria.repository.RolesDao;
import com.klinik.santamaria.repository.UsersDao;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UsersService {

  @Autowired
  private UsersDao usersDao;

  @Autowired
  private EmailService emailService;

  @Autowired
  private KeycloakService keycloakService;

  @Autowired
  private RolesDao rolesDao;

  @Value("${keycloak.resource}")
  private String clientId;

  @Transactional
  public Users createUsers(UsersDto usersDto) throws Exception {
        Optional<Users> users = usersDao.findByEmail(usersDto.getEmail());
        Users user = new Users();
        if (usersDto.getRole() == 0){
          throw new Exception("Position harus diisi");
        }else if (usersDto.getUsername() == null || usersDto.getUsername().isEmpty()){
          throw new Exception("username harus diisi");
        }else if (usersDto.getFullName() == null || usersDto.getFullName().isEmpty()) {
          throw new Exception("fullname harus diisi");
        }else if (usersDto.getEmail() == null || usersDto.getEmail().isEmpty()){
          throw new Exception("email harus diisi");
        }
        if (users.isEmpty()){
          String addUserToKeycloak = keycloakService.createUserToKeycloak(usersDto);
          if (!addUserToKeycloak.isEmpty()){
            user.setEmail(usersDto.getEmail());
            user.setUsername(usersDto.getUsername());
            user.setFullName(usersDto.getFullName());
            user.setPhoneNumber(usersDto.getPhoneNumber());
            user.setUserIdKeycloak(addUserToKeycloak);
            Role role = rolesDao.getById(usersDto.getRole());
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            user.setRoles(roles);
          }
        }else {
          throw new Exception("User already exists");
        }

        usersDao.save(user);

        emailService.sendEmail(usersDto);
        return  user;
  }

  public List<Role> getRoles(){
    List<Role> roles = rolesDao.findAll();
    return roles;
  }

  public String getToken(LoginDto loginDto) throws IOException {
    String token = keycloakService.getToken(loginDto);
    return token;
  }

  public UsersDto getUserDetail(HttpServletRequest request) throws IOException {
      AccessToken accessToken = getUserAccess(request);
      Optional<Users> users = usersDao.findByEmail(accessToken.getEmail());
      UsersDto usersDto = new UsersDto();
      if (!users.isEmpty()){
          usersDto.setUsername(users.get().getUsername());
          usersDto.setEmail(users.get().getEmail());
          usersDto.setFullName(users.get().getFullName());
          usersDto.setPhoneNumber(users.get().getPhoneNumber());
          usersDto.setRole(users.get().getRoles().stream().findFirst().get().getRoleId());
      }
      return usersDto;
  }

  private AccessToken getUserAccess(HttpServletRequest request) throws IOException {
    KeycloakAuthenticationToken principal = (KeycloakAuthenticationToken) request.getUserPrincipal();
    return principal.getAccount().getKeycloakSecurityContext().getToken();
  }
}
