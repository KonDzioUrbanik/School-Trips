package com.apiwosze.schooltrips.klasa; // Definicja pakietu dla modułu Klasy

import org.springframework.data.jpa.repository.JpaRepository; // Import interfejsu bazowego JPA
import org.springframework.stereotype.Repository; // Import adnotacji oznaczającej repozytorium
import java.util.Optional; // Import kontenera Optional

@Repository // Oznaczenie interfejsu jako komponentu dostępu do danych (Repository Bean)
public interface KlasaRepository extends JpaRepository<KlasaModel, Long> {
    
    // Sprawdza czy w bazie danych istnieje już klasa o podanej nazwie (zwraca true/false)
    boolean existsByNazwa(String name);

    // Usuwa klasę z bazy danych na podstawie jej nazwy
    void deleteByNazwa(String nazwa);

    // Wyszukuje klasę w bazie na podstawie jej nazwy i zwraca ją opakowaną w Optional
    Optional<KlasaModel> findByNazwa(String nazwa);
}
