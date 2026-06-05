package com.apiwosze.schooltrips.security; // Definicja pakietu bezpieczeństwa

import jakarta.persistence.*; // Import adnotacji JPA do mapowania obiektowo-relacyjnego
import lombok.Data; // Import Lombok @Data do automatycznego generowania getterów, setterów, toString, equals i hashCode
import org.springframework.security.core.GrantedAuthority; // Import klasy reprezentującej uprawnienie przyznane użytkownikowi
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Import implementacji uprawnienia tekstowego (np. ROLE_ADMIN)
import org.springframework.security.core.userdetails.UserDetails; // Import interfejsu Spring Security reprezentującego profil użytkownika

import java.util.Collection; // Import kolekcji Javy
import java.util.List; // Import listy Javy

@Data // Adnotacja Lombok generująca automatycznie metody (gettery, settery, toString, constructor itp.)
@Entity // Oznacza klasę jako encję JPA (mapowaną na tabelę w bazie danych)
@Table(name = "uzytkownik") // Określenie nazwy tabeli w bazie danych jako "uzytkownik"
public class Uzytkownik implements UserDetails { // Klasa implementuje UserDetails, by integrować się ze Spring Security

    @Id // Oznaczenie pola jako klucza głównego tabeli
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Automatyczne generowanie wartości klucza (auto-increment w bazie)
    private Long id;

    @Column(unique = true, nullable = false) // Pole w bazie musi być unikalne (np. uniemożliwia rejestrację 2 takich samych loginów) i niepuste
    private String username;

    @Column(nullable = false) // Pole hasła w bazie nie może być puste
    private String password;

    @Enumerated(EnumType.STRING) // Mapowanie enuma roli użytkownika do bazy danych jako tekst (zamiast wartości liczbowej)
    private RolaUzytkownika rola;

    // Metoda zwracająca uprawnienia (role) przypisane do użytkownika
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security domyślnie wymaga przedrostka "ROLE_" do autoryzacji roli (np. hasRole("ADMIN") szuka "ROLE_ADMIN")
        return List.of(new SimpleGrantedAuthority("ROLE_" + rola.name()));
    }

    // Metoda określająca czy konto użytkownika wygasło (zwracamy true czyli konto nigdy nie wygasa)
    @Override
    public boolean isAccountNonExpired() { return true; }

    // Metoda określająca czy konto użytkownika jest zablokowane (zwracamy true czyli konto jest zawsze odblokowane)
    @Override
    public boolean isAccountNonLocked() { return true; }

    // Metoda określająca czy poświadczenia (hasło) użytkownika wygasły (zwracamy true czyli hasło jest zawsze ważne)
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // Metoda określająca czy konto użytkownika jest aktywne (zwracamy true czyli jest zawsze aktywne i można się logować)
    @Override
    public boolean isEnabled() { return true; }
}