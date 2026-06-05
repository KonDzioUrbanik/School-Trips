package com.apiwosze.schooltrips.nauczyciel; // Definicja pakietu dla modułu Nauczycieli

import org.springframework.data.jpa.repository.JpaRepository; // Import bazowego interfejsu repozytorium Spring Data JPA
import org.springframework.stereotype.Repository; // Import adnotacji Spring Repository
import java.util.Optional; // Import kontenera Optional

@Repository // Oznaczenie jako repozytorium danych wstrzykiwane przez kontener Springa
public interface NauczycielRepository extends JpaRepository<NauczycielModel, Long> {
    
    // Sprawdza, czy istnieje nauczyciel o określonym imieniu i nazwisku (zwraca true/false)
    boolean existsByImieAndNazwisko(String imie, String nazwisko);

    // Usuwa nauczyciela z bazy danych na podstawie jego imienia i nazwiska
    void deleteByImieAndNazwisko(String imie, String nazwisko);

    // Wyszukuje nauczyciela w bazie po imieniu i nazwisku i zwraca go opakowanego w klasę Optional
    Optional<NauczycielModel> findByImieAndNazwisko(String imie, String nazwisko);
}
