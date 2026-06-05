package com.apiwosze.schooltrips.security; // Definicja pakietu bezpieczeństwa

import org.springframework.data.jpa.repository.JpaRepository; // Import interfejsu bazowego Spring Data JPA dla operacji CRUD i paginacji
import java.util.Optional; // Import kontenera Optional do bezpiecznej obsługi potencjalnych wartości null

// Interfejs repozytorium do zarządzania danymi użytkowników w bazie danych
// Rozszerzenie JpaRepository dostarcza automatyczną implementację metod takich jak save, findById, delete itp.
public interface UzytkownikRepository extends JpaRepository<Uzytkownik, Long> {
    
    // Sygnatura metody wyszukującej użytkownika po jego unikalnej nazwie (loginie)
    // Spring Data JPA automatycznie wygeneruje zapytanie SQL: SELECT * FROM uzytkownik WHERE username = ?
    Optional<Uzytkownik> findByUsername(String username);
}