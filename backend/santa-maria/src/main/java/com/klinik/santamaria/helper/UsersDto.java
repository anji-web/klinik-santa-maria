package com.klinik.santamaria.helper;

import lombok.Data;

@Data
public class UsersDto {
  private String username;
  private String fullName;
  private String email;
  private String password;
  private String phoneNumber;
  private int role;

}
