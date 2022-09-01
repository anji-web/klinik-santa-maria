package com.klinik.santamaria.controller;

import com.klinik.santamaria.helper.BaseDto;
import com.klinik.santamaria.helper.DiagnosaDto;
import com.klinik.santamaria.helper.LoginDto;
import com.klinik.santamaria.helper.PasienDto;
import com.klinik.santamaria.helper.UsersDto;
import com.klinik.santamaria.model.Diagnosa;
import com.klinik.santamaria.model.Pasien;
import com.klinik.santamaria.model.Role;
import com.klinik.santamaria.model.Users;
import com.klinik.santamaria.repository.DiagnosaDao;
import com.klinik.santamaria.repository.PasienDao;
import com.klinik.santamaria.repository.UsersDao;
import com.klinik.santamaria.service.ActionService;
import com.klinik.santamaria.service.KeycloakService;
import com.klinik.santamaria.service.UsersService;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.bouncycastle.pqc.asn1.ParSet;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("users")
public class UsersController {

  @Autowired
  private UsersService userService;

  @Autowired
  private UsersDao usersDao;

  @Autowired
  private ActionService actionService;

  @Autowired
  private KeycloakService keycloakService;

  @Autowired
  private PasienDao pasienDao;

  @Autowired
  private DiagnosaDao diagnosaDao;


  @PostMapping
  public ResponseEntity<Object> addUsers(@RequestBody UsersDto usersDto) {
    try {
            Users user = userService.createUsers(usersDto);
            return ResponseEntity.ok(user);
    }catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("list-users")
  public ResponseEntity<List<Users>> listUsers() {
    return ResponseEntity.ok(usersDao.findAll());
  }

  @DeleteMapping("delete-user")
  @Transactional
  public ResponseEntity<Object> deleteUsers(@RequestParam("id") Long id){
    try {
      Users users = usersDao.getById(id);
      boolean deleteUserInKeycloak = keycloakService.deleteUser(users.getUserIdKeycloak());
      if(deleteUserInKeycloak) {
        usersDao.deletUserRole(users.getUserId());
        usersDao.delete(users);
        return ResponseEntity.ok().body("delete pasien berhasil");
      }else{
        return ResponseEntity.ok().body("User tidak dapat dihapus");
      }
    }catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }


  @GetMapping("list-roles")
  public ResponseEntity<List<Role>> listRoles() {
          return ResponseEntity.ok(userService.getRoles());
  }

  @PostMapping("login")
  public ResponseEntity<String> login(@RequestBody LoginDto loginDto) {
    try {
            return ResponseEntity.ok(userService.getToken(loginDto));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("user-detail")
  public ResponseEntity<Object> userDetail(HttpServletRequest request){
    try {
            return ResponseEntity.ok(userService.getUserDetail(request));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
  }

  @PostMapping("add-pasien")
  public ResponseEntity<BaseDto> addPasien(@RequestBody Pasien pasien){
    try {
            actionService.addPasien(pasien);
            BaseDto result = new BaseDto();
            result.setStatusCode(HttpStatus.CREATED.value());
            result.setStatusMessage(HttpStatus.CREATED.toString());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }catch (Exception e) {
            BaseDto result = new BaseDto();
            result.setStatusCode(HttpStatus.BAD_REQUEST.value());
            result.setStatusMessage(e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
  }

  @PostMapping("add-diagnosa")
  public ResponseEntity<BaseDto> addDiagnosa(@RequestBody Diagnosa diagnosa){
    try {
      actionService.addDiagnosa(diagnosa);
      BaseDto result = new BaseDto();
      result.setStatusCode(HttpStatus.CREATED.value());
      result.setStatusMessage(HttpStatus.CREATED.toString());
      return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }catch (Exception e) {
      BaseDto result = new BaseDto();
      result.setStatusCode(HttpStatus.BAD_REQUEST.value());
      result.setStatusMessage(e.getMessage());
      return ResponseEntity.badRequest().body(result);
    }
  }

  @GetMapping("pasien")
  public ResponseEntity<Object> getPasien(){
      try {
        List<Pasien> pasienList = pasienDao.findAll();
        List<PasienDto> pasienDtos = new ArrayList<>();
        int no = 1;
        for (Pasien p : pasienList) {
          PasienDto pasienDto = new PasienDto();
          pasienDto.setPasienId(p.getPasienId());
          pasienDto.setFullname(p.getFullname());
          pasienDto.setAddress(p.getAddress());
          pasienDto.setNoRekamMedik(p.getNoRekamMedik());
          pasienDto.setBirthPlace(p.getBirthPlace());
          pasienDto.setBirthDate(p.getBirthDate());
          pasienDto.setGender(p.getGender());
          pasienDto.setPhoneNumber(p.getPhoneNumber());
          pasienDto.setStatus(p.getStatus());

          pasienDtos.add(pasienDto);
        }
        return ResponseEntity.ok(pasienDtos);
      }catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("diagnosa")
  public ResponseEntity<Object> getDiagnosa(){
    try {

      List<Pasien> pasienList = pasienDao.findAll();
      List<DiagnosaDto> diagnosaDaoList = new ArrayList<>();
      for (Pasien pasien : pasienList){
          DiagnosaDto diagnosaDto = diagnosaDao.getDiagnosaList(pasien.getPasienId());
          diagnosaDaoList.add(diagnosaDto);
      }
      return ResponseEntity.ok(diagnosaDaoList);
    }catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @DeleteMapping("delete-pasien")
  public ResponseEntity<Object> delete(@RequestParam("id") Long id){
    try {
      Pasien pasien = pasienDao.getById(id);
      Diagnosa diagnosa = diagnosaDao.getDiagnosaByPasienId(pasien.getPasienId());
      if (diagnosa != null) {
        diagnosaDao.delete(diagnosa);
      }
      pasienDao.delete(pasien);
      return ResponseEntity.ok().body("delete pasien berhasil");
    }catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("get-pasien")
  public ResponseEntity<Object> get(@RequestParam("id") Long id){
    try {
      Pasien pasien = pasienDao.getById(id);
      PasienDto pasienDto =  new PasienDto();
      pasienDto.setPasienId(pasien.getPasienId());
      pasienDto.setFullname(pasien.getFullname());
      pasienDto.setAddress(pasien.getAddress());
      pasienDto.setNoRekamMedik(pasien.getNoRekamMedik());
      pasienDto.setBirthPlace(pasien.getBirthPlace());
      pasienDto.setBirthDate(pasien.getBirthDate());
      pasienDto.setGender(pasien.getGender());
      pasienDto.setPhoneNumber(pasien.getPhoneNumber());
      pasienDto.setStatus(pasien.getStatus());
      return ResponseEntity.ok().body(pasienDto);
    }catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping("edit-pasien")
  public ResponseEntity<Object> edit(@RequestParam("id") Long id,@RequestBody Pasien pasienModel ){
    try {
      Pasien pasien = pasienDao.getById(id);
      pasien.setNoRekamMedik(pasienModel.getNoRekamMedik());
      pasien.setAddress(pasienModel.getAddress());
      pasien.setStatus(pasienModel.getStatus());
      pasien.setBirthDate(pasienModel.getBirthDate());
      pasien.setBirthPlace(pasienModel.getBirthPlace());
      pasien.setFullname(pasienModel.getFullname());
      pasien.setGender(pasienModel.getGender());
      pasien.setPhoneNumber(pasienModel.getPhoneNumber());
      pasienDao.save(pasien);

      return ResponseEntity.ok().body(pasienModel);

    }catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}
