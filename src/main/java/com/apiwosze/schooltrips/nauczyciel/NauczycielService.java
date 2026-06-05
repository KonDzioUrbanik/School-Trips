package com.apiwosze.schooltrips.nauczyciel; // Definicja pakietu dla modułu Nauczycieli

import jakarta.transaction.Transactional; // Import adnotacji transakcyjności do operacji modyfikujących bazę (np. delete)
import org.springframework.stereotype.Service; // Import adnotacji Spring Service
import java.util.List; // Import klasy listy Javy

@Service // Oznaczenie klasy jako usługi logiki biznesowej dla nauczycieli
public class NauczycielService {
    private final NauczycielRepository nauczycielRepository; // Wstrzykiwane repozytorium do operacji na bazie danych

    // Konstruktor wstrzykujący repozytorium nauczycieli
    public NauczycielService(NauczycielRepository nauczycielRepository) {
        this.nauczycielRepository = nauczycielRepository;
    }

    // Metoda rejestrująca nowego nauczyciela w bazie danych
    public NauczycielModel createNauczyciel(NauczycielDto nauczycielDto) {
        // Weryfikacja, czy nauczyciel o podanym imieniu i nazwisku już istnieje
        if (nauczycielRepository.existsByImieAndNazwisko(nauczycielDto.imie(), nauczycielDto.nazwisko())) {
            // Jeśli istnieje, rzuć błąd walidacji biznesowej
            throw new IllegalArgumentException("Nauczyciel już istnieje!");
        } else {
            // W przeciwnym razie stwórz nowy obiekt modelu, uzupełnij go i zapisz w bazie
            NauczycielModel nauczycielModel = new NauczycielModel();
            nauczycielModel.setImie(nauczycielDto.imie());
            nauczycielModel.setNazwisko(nauczycielDto.nazwisko());
            nauczycielModel.setPrzedmiot(nauczycielDto.przedmiot());
            nauczycielModel.setTelefon_kontaktowy(nauczycielDto.telefon_kontaktowy());
            return nauczycielRepository.save(nauczycielModel); // Zwrócenie zapisanego obiektu z wygenerowanym ID
        }
    }

    // Metoda usuwająca nauczyciela z bazy danych na podstawie imienia i nazwiska
    @Transactional // Oznaczenie transakcji niezbędne przy własnych operacjach usuwających (np. deleteByImieAndNazwisko)
    public void deleteNauczyciel(NauczycielDto nauczycielDto) {
        // Sprawdzenie czy nauczyciel istnieje w bazie danych
        if (!nauczycielRepository.existsByImieAndNazwisko(nauczycielDto.imie(), nauczycielDto.nazwisko())) {
            throw new IllegalArgumentException("Nie ma takiego nauczyciela!");
        } else {
            // Usunięcie nauczyciela z bazy danych
            nauczycielRepository.deleteByImieAndNazwisko(nauczycielDto.imie(), nauczycielDto.nazwisko());
        }
    }

    // Metoda zwracająca listę wszystkich nauczycieli w systemie
    public List<NauczycielModel> getAllNauczyciele() {
        return nauczycielRepository.findAll(); // Pobranie wszystkich wierszy z tabeli nauczyciel
    }

    // Metoda aktualizująca nauczany przedmiot oraz numer telefonu na podstawie imienia i nazwiska nauczyciela
    public NauczycielModel updatePrzedmiotAndTelefonNauczyciel(NauczycielDto nauczycielDto){
        // Pobranie nauczyciela po imieniu i nazwisku lub zgłoszenie błędu
        NauczycielModel nauczycielModelEdit = nauczycielRepository.findByImieAndNazwisko(nauczycielDto.imie(), nauczycielDto.nazwisko())
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego nauczyciela!"));
        
        nauczycielModelEdit.setPrzedmiot(nauczycielDto.przedmiot()); // Aktualizacja przedmiotu
        nauczycielModelEdit.setTelefon_kontaktowy(nauczycielDto.telefon_kontaktowy()); // Aktualizacja telefonu
        return nauczycielRepository.save(nauczycielModelEdit); // Zapisanie zmian w bazie
    }

    // Metoda aktualizująca imię i nazwisko nauczyciela na podstawie jego klucza głównego (ID)
    public NauczycielModel updateImieAndNazwiskoNauczyciela(Long id, NauczycielDto nauczycielDto){
        // Wyszukanie nauczyciela po kluczu głównym (ID) lub zgłoszenie błędu
        NauczycielModel nauczycielModelEdit = nauczycielRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego nauczyciela!"));
        
        nauczycielModelEdit.setImie(nauczycielDto.imie()); // Aktualizacja imienia
        nauczycielModelEdit.setNazwisko(nauczycielDto.nazwisko()); // Aktualizacja nazwiska
        return nauczycielRepository.save(nauczycielModelEdit); // Zapisanie zmian w bazie
    }

    // Metoda pobierająca szczegółowe dane pojedynczego nauczyciela na podstawie klucza głównego (ID)
    public NauczycielModel getNauczycielById(Long id){
        return nauczycielRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego nauczyciela!"));
    }
}
