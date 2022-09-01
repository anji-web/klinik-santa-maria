package com.klinik.santamaria.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Pasien {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long pasienId;

  private String noRekamMedik;

  private String fullname;

  private String birthPlace;

  @JsonFormat(shape = JsonFormat.Shape.STRING ,pattern="yyyy-MM-dd")
  private Date birthDate;

  private String phoneNumber;

  private String gender;

  private String address;

  private  String status;

}
