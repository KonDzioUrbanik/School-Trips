package com.apiwosze.schooltrips.security; // Definicja pakietu bezpieczeństwa

import org.springframework.boot.CommandLineRunner; // Import interfejsu pozwalającego na uruchamianie kodu podczas startu aplikacji Spring Boot
import org.springframework.security.crypto.password.PasswordEncoder; // Import interfejsu służącego do kodowania haseł
import org.springframework.stereotype.Component; // Import adnotacji Spring Component

@Component // Klasa jest oznaczona jako komponent i automatycznie wykrywana oraz rejestrowana jako Bean
public class DataInitializer implements CommandLineRunner {

    private final UzytkownikRepository uzytkownikRepository; // Repozytorium do zapisu użytkowników w bazie
    private final PasswordEncoder passwordEncoder; // Koder haseł do bezpiecznego haszowania haseł testowych użytkowników

    // Konstruktor wstrzykujący repozytorium oraz encoder haseł z kontekstu Springa
    public DataInitializer(UzytkownikRepository uzytkownikRepository, PasswordEncoder passwordEncoder) {
        this.uzytkownikRepository = uzytkownikRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Metoda run wywoływana automatycznie tuż po całkowitym załadowaniu kontekstu Spring Boot
    @Override
    public void run(String... args) {
        // Sprawdzamy czy tabela użytkowników w bazie jest pusta
        if (uzytkownikRepository.count() == 0) {

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
            uzytkownikRepository.save(uczen); // Zapisanie ucznia w bazie

            // Wypisanie komunikatu diagnostycznego na konsoli aplikacji
            System.out.println("Utworzono domyślne konta testowe.");
        }
    }
}