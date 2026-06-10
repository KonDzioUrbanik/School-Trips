package com.apiwosze.schooltrips.security; // Definicja pakietu bezpieczeństwa

import jakarta.transaction.Transactional; // Import adnotacji do zarządzania transakcjami
import org.springframework.beans.factory.annotation.Value; // Import adnotacji do wstrzykiwania właściwości konfiguracyjnych
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Import wyjątku braku użytkownika
import org.springframework.stereotype.Service; // Import adnotacji Spring Service

import java.time.Instant; // Import klasy daty i czasu precyzyjnego UTC
import java.util.Optional; // Import kontenera Optional
import java.util.UUID; // Import klasy generującej losowe identyfikatory UUID (Universal Unique Identifier)

@Service // Rejestracja klasy jako serwisu logiki biznesowej dla Refresh Tokenów
public class RefreshTokenService {

    // Wstrzyknięcie czasu ważności tokenu odświeżania w milisekundach (domyślnie 7 dni: 604800000 ms)
    @Value("${jwt.refreshExpirationMs:604800000}")
    private Long refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository; // Referencja do repozytorium tokenów odświeżania
    private final UzytkownikRepository uzytkownikRepository; // Referencja do repozytorium użytkowników

    // Konstruktor wstrzykujący repozytoria
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UzytkownikRepository uzytkownikRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.uzytkownikRepository = uzytkownikRepository;
    }

    // Wyszukanie tokenu w bazie na podstawie jego wartości tekstowej
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // Generowanie i zapisanie nowego Refresh Tokenu dla użytkownika o podanym loginie
    @Transactional
    public RefreshToken createRefreshToken(String username) {
        // Wyszukanie użytkownika w bazie danych
        Uzytkownik user = uzytkownikRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika: " + username));

        // Usunięcie ewentualnego istniejącego tokenu użytkownika (wymuszamy 1 aktywny Refresh Token na usera)
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush(); // Wymuszenie zapisu usunięcia przed wstawieniem nowej wartości (zapobiega konfliktom 409)

        // Utworzenie nowej encji RefreshToken
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user); // Ustawienie powiązanego użytkownika
        // Obliczenie daty wygaśnięcia: obecny moment + czas ważności
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        // Generowanie losowej, unikalnej wartości UUID jako wartości tokenu
        refreshToken.setToken(UUID.randomUUID().toString());

        // Zapisanie tokenu w bazie danych i zwrócenie go
        return refreshTokenRepository.save(refreshToken);
    }

    // Metoda weryfikująca czy dany Refresh Token nie wygasł
    public RefreshToken verifyExpiration(RefreshToken token) {
        // Porównanie czasu wygaśnięcia tokenu z aktualnym czasem systemowym
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            // Jeśli wygasł, usuń go z bazy danych
            refreshTokenRepository.delete(token);
            // Rzuć błąd uniemożliwiający wygenerowanie nowego Access Tokenu
            throw new RuntimeException("Refresh token wygasł. Zaloguj się ponownie.");
        }
        return token; // Zwrócenie poprawnego tokenu
    }

    // Usunięcie tokenu odświeżania na podstawie nazwy użytkownika (np. przy wylogowywaniu)
    @Transactional
    public int deleteByUsername(String username) {
        // Wyszukanie użytkownika lub zgłoszenie błędu
        Uzytkownik user = uzytkownikRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika: " + username));
        // Usunięcie tokenu przypisanego do użytkownika
        return refreshTokenRepository.deleteByUser(user);
    }
}
