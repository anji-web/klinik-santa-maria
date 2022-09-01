package com.klinik.santamaria.repository;

import com.klinik.santamaria.model.Users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersDao extends JpaRepository<Users, Long> {
  Optional<Users> findByEmail(String email);

  @Query(value = "delete from user_roles where user_id = ?1", nativeQuery = true)
  @Modifying
  void deletUserRole(Long userId);
}
