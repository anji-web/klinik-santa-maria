package com.klinik.santamaria.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Diagnosa {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long diagnosaId;

  private String keluhan;

  private String diagnosa;

  private String namaObat;

  private int jumlahObat;

  private Long pasienId;

}
