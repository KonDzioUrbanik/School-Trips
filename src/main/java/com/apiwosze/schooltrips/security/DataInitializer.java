package com.apiwosze.schooltrips.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UzytkownikRepository uzytkownikRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UzytkownikRepository uzytkownikRepository, PasswordEncoder passwordEncoder) {
        this.uzytkownikRepository = uzytkownikRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Tworzymy konta testowe, jeśli baza jest pusta
        if (uzytkownikRepository.count() == 0) {

            Uzytkownik admin = new Uzytkownik();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // hasło w bazie będzie zahaszowane
            admin.setRola(RolaUzytkownika.ADMIN);
            uzytkownikRepository.save(admin);

            Uzytkownik nauczyciel = new Uzytkownik();
            nauczyciel.setUsername("nauczyciel");
            nauczyciel.setPassword(passwordEncoder.encode("nauczyciel123"));
            nauczyciel.setRola(RolaUzytkownika.NAUCZYCIEL);
            uzytkownikRepository.save(nauczyciel);

            Uzytkownik uczen = new Uzytkownik();
            uczen.setUsername("uczen");
            uczen.setPassword(passwordEncoder.encode("uczen123"));
            uczen.setRola(RolaUzytkownika.UCZEN_RODZIC);
            uzytkownikRepository.save(uczen);

            System.out.println("Utworzono domyślne konta testowe.");
        }
    }
}