package com.apiwosze.schooltrips.security; // Definicja pakietu bezpieczeństwa

import org.springframework.security.core.userdetails.UserDetails; // Import interfejsu reprezentującego dane zalogowanego użytkownika
import org.springframework.security.core.userdetails.UserDetailsService; // Import interfejsu Spring Security służącego do ładowania poświadczeń
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Import wyjątku rzucanego gdy login nie istnieje
import org.springframework.stereotype.Service; // Import adnotacji Spring Service

@Service // Rejestracja klasy jako serwisu w kontenerze Springa
public class CustomUserDetailsService implements UserDetailsService {

    private final UzytkownikRepository uzytkownikRepository; // Deklaracja repozytorium do dostępu do bazy danych użytkowników

    // Konstruktor wstrzykujący repozytorium użytkowników
    public CustomUserDetailsService(UzytkownikRepository uzytkownikRepository) {
        this.uzytkownikRepository = uzytkownikRepository;
    }

    // Nadpisanie wymaganej metody loadUserByUsername z interfejsu UserDetailsService
    // Służy ona Spring Security do weryfikacji tożsamości podczas logowania oraz autoryzacji tokenu
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Wyszukanie użytkownika w bazie po unikalnym loginie (username)
        // Jeśli użytkownik o podanej nazwie nie istnieje, rzucany jest dedykowany wyjątek UsernameNotFoundException
        return uzytkownikRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika: " + username));
    }
}