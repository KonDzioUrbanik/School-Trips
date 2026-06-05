package com.apiwosze.schooltrips.klasa; // Definicja pakietu dla modułu Klasy

import org.springframework.stereotype.Service; // Import adnotacji oznaczającej klasę jako serwis
import org.springframework.transaction.annotation.Transactional; // Import adnotacji do zarządzania transakcjami bazy danych
import java.util.List; // Import listy Javy

@Service // Oznaczenie klasy jako usługi (logika biznesowa), która zostanie zarejestrowana jako Bean w kontenerze Springa
public class KlasaService {

    private final KlasaRepository klasaRepository; // Repozytorium bazy danych wstrzykiwane przez konstruktor

    // Konstruktor wstrzykujący repozytorium do klasy serwisowej
    public KlasaService(KlasaRepository klasaRepository) {
        this.klasaRepository = klasaRepository;
    }

    // Metoda tworząca nową klasę w bazie danych na podstawie obiektu DTO
    public KlasaModel createKlasa(KlasaDto klasaDto) {
        // Sprawdzenie, czy klasa o podanej nazwie już istnieje w bazie danych
        if (klasaRepository.existsByNazwa(klasaDto.nazwa())){
            // Jeśli istnieje, rzucany jest wyjątek błędu walidacji biznesowej
            throw new IllegalArgumentException("Klasa o takiej nazwie już istnieje!");
        }
        else {
            // Jeśli nie istnieje, tworzona jest nowa encja KlasaModel, uzupełniane są dane i następuje zapis do bazy
            KlasaModel klasaModel = new KlasaModel();
            klasaModel.setNazwa(klasaDto.nazwa());
            klasaModel.setProfil(klasaDto.profil());
            return klasaRepository.save(klasaModel); // Metoda save zwraca zapisaną encję wraz z nadanym ID bazy
        }
    }

    // Metoda usuwająca klasę z bazy danych na podstawie nazwy klasy
    @Transactional // Wymagane przez Hibernate/JPA przy usuwaniu obiektów za pomocą zapytań własnych (np. nie po ID)
    public void deleteKlasa(KlasaDto klasaDto){
        // Sprawdzenie czy usuwana klasa w ogóle istnieje w bazie danych
        if(!klasaRepository.existsByNazwa(klasaDto.nazwa())){
            // Rzucenie wyjątku w przypadku braku rekordu
            throw new IllegalArgumentException("Nie ma takiej klasy!");
        }
        else {
            // Usunięcie klasy za pomocą metody repozytorium
            klasaRepository.deleteByNazwa(klasaDto.nazwa());
        }
    }

    // Metoda pobierająca listę wszystkich zarejestrowanych klas z bazy danych
    public List<KlasaModel> getAllKlasy(){
        return klasaRepository.findAll(); // Wywołanie wbudowanej metody pobierającej wszystkie wiersze z tabeli
    }

    // Metoda modyfikująca profil klasy na podstawie jej unikalnej nazwy
    public KlasaModel updateProfilKlasy(KlasaDto klasaDto){
        // Pobranie klasy z bazy danych po nazwie lub zgłoszenie błędu jeśli nie istnieje
        KlasaModel klasaModelEdit = klasaRepository.findByNazwa(klasaDto.nazwa())
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiej klasy!"));
        
        klasaModelEdit.setProfil(klasaDto.profil()); // Aktualizacja pola profilu w obiekcie
        return klasaRepository.save(klasaModelEdit); // Zapisanie zmian w bazie danych
    }

    // Metoda modyfikująca nazwę klasy na podstawie jej klucza głównego (ID)
    public KlasaModel updateNazwyKlasy(Long id, KlasaDto klasaDto){
        // Pobranie klasy po kluczu głównym (ID) lub zgłoszenie błędu
        KlasaModel klasaModelEdit = klasaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiej klasy!"));
        
        klasaModelEdit.setNazwa(klasaDto.nazwa()); // Aktualizacja nazwy klasy w obiekcie
        return klasaRepository.save(klasaModelEdit); // Zapisanie zmian w bazie danych
    }
}
