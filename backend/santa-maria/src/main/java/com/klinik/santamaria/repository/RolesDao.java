package com.klinik.santamaria.repository;

import com.klinik.santamaria.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolesDao extends JpaRepository<Role, Integer> {
  Role findByRoleName(String roleName);
}
