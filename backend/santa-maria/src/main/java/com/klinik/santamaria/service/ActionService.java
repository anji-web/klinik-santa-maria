package com.klinik.santamaria.service;

import com.klinik.santamaria.helper.BaseDto;
import com.klinik.santamaria.model.Diagnosa;
import com.klinik.santamaria.model.Pasien;
import com.klinik.santamaria.repository.DiagnosaDao;
import com.klinik.santamaria.repository.PasienDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActionService {

  @Autowired
  private PasienDao pasienDao;

  @Autowired
  private DiagnosaDao diagnosaDao;

  public void addPasien(Pasien pasien) throws Exception {
      if (pasien.getNoRekamMedik() == null || pasien.getNoRekamMedik().isEmpty()){
        throw new Exception("no rekam medik tidak boleh kosong");
      }

      if (pasien.getFullname() == null || pasien.getFullname().isEmpty()){
        throw new Exception("nama pasien tidak boleh kosong");
      }
      pasien.setStatus("Dalam Antrian");
      pasienDao.save(pasien);
  }

  public void addDiagnosa(Diagnosa diagosa) {
    Pasien pasien = pasienDao.getById(diagosa.getPasienId());
    pasien.setStatus("Sudah Diperiksa");
    diagnosaDao.save(diagosa);
  }

}
