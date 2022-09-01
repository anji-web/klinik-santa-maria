package com.klinik.santamaria.repository;

import com.klinik.santamaria.model.Pasien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasienDao extends JpaRepository<Pasien, Long> {
}
