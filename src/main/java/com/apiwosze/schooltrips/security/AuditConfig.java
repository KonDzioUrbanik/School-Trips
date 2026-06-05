package com.apiwosze.schooltrips.security; // Definicja pakietu bezpieczeństwa

import org.springframework.context.annotation.Configuration; // Import adnotacji Spring Configuration
import org.springframework.data.domain.AuditorAware; // Import interfejsu dostarczającego nazwę aktualnego audytora
import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // Import adnotacji włączającej automatyczne śledzenie zmian w JPA
import org.springframework.security.core.Authentication; // Import reprezentacji uwierzytelnienia Spring Security
import org.springframework.security.core.context.SecurityContextHolder; // Import klasy dostępowej do kontekstu bezpieczeństwa

import java.util.Optional; // Import kontenera Optional

@Configuration // Oznaczenie klasy jako konfiguracyjnej dla kontekstu Springa
@EnableJpaAuditing // Włączenie mechanizmu JPA Auditing (automatyczne uzupełnianie dat i użytkowników w bazie)
public class AuditConfig implements AuditorAware<String> { // Klasa implementuje AuditorAware dostarczający dane jako String (username)

    // Metoda pobierająca login aktualnego użytkownika, który wywołał operację zapisu/edycji
    @Override
    public Optional<String> getCurrentAuditor() {
        // Pobranie obiektu uwierzytelnienia z wątku bieżącego żądania HTTP
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Jeśli nikt nie jest zalogowany (np. aplikacja startuje, działa skrypt inicjalizujący lub żądanie jest anonimowe)
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of("SYSTEM"); // Zwrócenie domyślnej nazwy "SYSTEM"
        }

        // Zwrócenie loginu zalogowanego użytkownika (np. "admin", "nauczyciel")
        return Optional.of(authentication.getName());
    }
}
