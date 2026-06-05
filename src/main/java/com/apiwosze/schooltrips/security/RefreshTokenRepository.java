package com.apiwosze.schooltrips.security; // Definicja pakietu bezpieczeństwa

import org.springframework.data.jpa.repository.JpaRepository; // Import bazowego repozytorium JPA
import org.springframework.data.jpa.repository.Modifying; // Import adnotacji oznaczającej operację modyfikującą bazę danych (np. DELETE/UPDATE)
import org.springframework.stereotype.Repository; // Import adnotacji Spring Repository
import java.util.Optional; // Import kontenera Optional

@Repository // Oznaczenie jako komponent dostępu do danych
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    // Wyszukanie encji RefreshToken na podstawie wartości tekstowej tokenu
    Optional<RefreshToken> findByToken(String token);

    // Adnotacja informująca Hibernate o konieczności wykonania zapytania modyfikującego
    @Modifying
    // Usunięcie tokenu powiązanego z konkretnym użytkownikiem (używane przy wylogowywaniu)
    int deleteByUser(Uzytkownik user);
}
