package com.klinik.santamaria.repository;

import com.klinik.santamaria.helper.DiagnosaDto;
import com.klinik.santamaria.model.Diagnosa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosaDao extends JpaRepository<Diagnosa, Long> {

  @Query(value = "SELECT p.pasien_id as pasienId, d.diagnosa_id as diagnosaId," +
      " p.fullname as name , " +
      " p.no_rekam_medik as noRekamMedik, " +
      "d.keluhan as keluhan, " +
      "d.diagnosa as diagnosa, " +
      "d.nama_obat as namaObat, " +
      "d.jumlah_obat as jumlahObat " +
      " FROM diagnosa d join pasien p on d.pasien_id = p.pasien_id WHERE d.pasien_id = ?1", nativeQuery = true)
  public DiagnosaDto getDiagnosaList(Long pasienId);

  public Diagnosa getDiagnosaByPasienId(Long pasienId);
}
