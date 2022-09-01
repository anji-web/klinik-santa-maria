package com.klinik.santamaria.helper;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Data;

@Data
public class PasienDto {
  private  int noUrut;

  private Long pasienId;

  private String noRekamMedik;

  private String fullname;

  private String birthPlace;

  @JsonFormat(shape = JsonFormat.Shape.STRING ,pattern="yyyy-MM-dd")
  private Date birthDate;

  private String phoneNumber;

  private String gender;

  private String status;

  private String address;
}
