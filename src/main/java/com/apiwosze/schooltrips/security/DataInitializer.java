package com.apiwosze.schooltrips.security; // Definicja pakietu bezpieczeństwa

import org.springframework.boot.CommandLineRunner; // Import interfejsu pozwalającego na uruchamianie kodu podczas startu aplikacji Spring Boot
import org.springframework.security.crypto.password.PasswordEncoder; // Import interfejsu służącego do kodowania haseł
import org.springframework.stereotype.Component; // Import adnotacji Spring Component

@Component // Klasa jest oznaczona jako komponent i automatycznie wykrywana oraz rejestrowana jako Bean
public class DataInitializer implements CommandLineRunner {

    private final UzytkownikRepository uzytkownikRepository; // Repozytorium do zapisu użytkowników w bazie
    private final PasswordEncoder passwordEncoder; // Koder haseł do bezpiecznego haszowania haseł testowych użytkowników
    private final com.apiwosze.schooltrips.klasa.KlasaRepository klasaRepository;
    private final com.apiwosze.schooltrips.uczen.UczenRepository uczenRepository;

    // Konstruktor wstrzykujący repozytoria oraz encoder haseł z kontekstu Springa
    public DataInitializer(UzytkownikRepository uzytkownikRepository, PasswordEncoder passwordEncoder,
                           com.apiwosze.schooltrips.klasa.KlasaRepository klasaRepository,
                           com.apiwosze.schooltrips.uczen.UczenRepository uczenRepository) {
        this.uzytkownikRepository = uzytkownikRepository;
        this.passwordEncoder = passwordEncoder;
        this.klasaRepository = klasaRepository;
        this.uczenRepository = uczenRepository;
    }

    // Metoda run wywoływana automatycznie tuż po całkowitym załadowaniu kontekstu Spring Boot
    @Override
    public void run(String... args) {
        // Sprawdzamy czy tabela użytkowników w bazie jest pusta
        if (uzytkownikRepository.count() == 0) {

            // Inicjalizacja domyślnej klasy
            com.apiwosze.schooltrips.klasa.KlasaModel klasaDefault = new com.apiwosze.schooltrips.klasa.KlasaModel();
            klasaDefault.setNazwa("1A");
            klasaDefault.setProfil("Matematyczno-Fizyczny");
            klasaDefault = klasaRepository.save(klasaDefault);

            // 1. Tworzenie użytkownika z rolą ADMIN
            Uzytkownik admin = new Uzytkownik(); // Instancjonowanie nowego użytkownika
            admin.setUsername("admin"); // Ustawienie loginu
            admin.setPassword(passwordEncoder.encode("admin123")); // Bezpieczne zahaszowanie hasła przed zapisem do bazy
            admin.setRola(RolaUzytkownika.ADMIN); // Przypisanie roli ADMIN
            uzytkownikRepository.save(admin); // Zapisanie admina w bazie danych

            // 2. Tworzenie użytkownika z rolą NAUCZYCIEL
            Uzytkownik nauczyciel = new Uzytkownik(); // Nowa instancja
            nauczyciel.setUsername("nauczyciel"); // Ustawienie loginu
            nauczyciel.setPassword(passwordEncoder.encode("nauczyciel123")); // Zahaszowanie hasła
            nauczyciel.setRola(RolaUzytkownika.NAUCZYCIEL); // Przypisanie roli NAUCZYCIEL
            uzytkownikRepository.save(nauczyciel); // Zapisanie nauczyciela w bazie

            // 3. Tworzenie użytkownika z rolą UCZEN_RODZIC (Uczeń / Rodzic)
            Uzytkownik uczen = new Uzytkownik(); // Nowa instancja
            uczen.setUsername("uczen"); // Ustawienie loginu
            uczen.setPassword(passwordEncoder.encode("uczen123")); // Zahaszowanie hasła
            uczen.setRola(RolaUzytkownika.UCZEN_RODZIC); // Przypisanie roli UCZEN_RODZIC
            uczen = uzytkownikRepository.save(uczen); // Zapisanie ucznia w bazie

            // Tworzenie profilu ucznia skojarzonego z kontem "uczen"
            com.apiwosze.schooltrips.uczen.UczenModel uczenModel = new com.apiwosze.schooltrips.uczen.UczenModel();
            uczenModel.setImie("Jan");
            uczenModel.setNazwisko("Kowalski");
            uczenModel.setData_urodzenia(java.time.LocalDate.of(2010, 5, 12));
            uczenModel.setKlasa(klasaDefault);
            uczenModel.setUser(uczen);
            uczenRepository.save(uczenModel);

            // Wypisanie komunikatu diagnostycznego na konsoli aplikacji
            System.out.println("Utworzono domyślne konta testowe.");
        }
    }
}