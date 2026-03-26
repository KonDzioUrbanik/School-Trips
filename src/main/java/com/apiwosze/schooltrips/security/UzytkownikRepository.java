package com.apiwosze.schooltrips.security;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UzytkownikRepository extends JpaRepository<Uzytkownik, Long> {
    Optional<Uzytkownik> findByUsername(String username);
}