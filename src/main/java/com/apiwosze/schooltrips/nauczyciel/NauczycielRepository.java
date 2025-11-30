package com.apiwosze.schooltrips.nauczyciel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NauczycielRepository extends JpaRepository<NauczycielModel, Long> {
    boolean existsByImieAndNazwisko(String imie, String nazwisko);
    void deleteByImieAndNazwisko(String imie, String nazwisko);
    Optional<NauczycielModel> findByImieAndNazwisko(String imie, String nazwisko);
}
