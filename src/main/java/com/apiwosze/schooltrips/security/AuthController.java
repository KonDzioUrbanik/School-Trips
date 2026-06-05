package com.apiwosze.schooltrips.security; // Definicja pakietu bezpieczeństwa

import jakarta.servlet.http.Cookie; // Import klasy reprezentującej plik cookie HTTP
import jakarta.servlet.http.HttpServletResponse; // Import interfejsu odpowiedzi HTTP
import jakarta.validation.Valid; // Import adnotacji do walidacji DTO
import jakarta.validation.constraints.NotBlank; // Import adnotacji walidacyjnej niepustego pola
import org.springframework.http.ResponseEntity; // Import klasy reprezentującej odpowiedź HTTP
import org.springframework.security.authentication.AuthenticationManager; // Import menedżera uwierzytelniania Spring Security
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Import klasy tokenu logowania login/hasło
import org.springframework.security.core.Authentication; // Import reprezentacji uwierzytelnionego użytkownika
import org.springframework.security.core.context.SecurityContextHolder; // Import klasy przechowującej kontekst zalogowanego wątku
import org.springframework.security.core.userdetails.UserDetails; // Import interfejsu szczegółów użytkownika
import org.springframework.web.bind.annotation.*; // Import adnotacji REST Spring MVC

@RestController // Kontroler REST zwracający odpowiedzi jako JSON
@RequestMapping("/api/auth") // Bazowa ścieżka API dla autoryzacji
public class AuthController {

    private final AuthenticationManager authenticationManager; // Deklaracja menedżera uwierzytelniania
    private final CustomUserDetailsService userDetailsService; // Serwis pobierający użytkownika z bazy
    private final JwtService jwtService; // Serwis obsługujący Access Tokeny JWT
    private final RefreshTokenService refreshTokenService; // Serwis obsługujący Refresh Tokeny

    // Konstruktor wstrzykujący zależności (w tym serwis Refresh Tokenów)
    public AuthController(AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService,
                          JwtService jwtService,
                          RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    // Rekord reprezentujący żądanie logowania z walidacją wejściową
    public record AuthRequest(
            @NotBlank(message = "Nazwa użytkownika nie może być pusta")
            String username,
            @NotBlank(message = "Hasło nie może być puste")
            String password
    ) {}

    // Rekord reprezentujący odpowiedź zawierającą Access Token JWT
    public record AuthResponse(String token) {}

    // Metoda logowania (POST /api/auth/login) generująca Access Token oraz Refresh Token (zapisywany w ciasteczku)
    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid AuthRequest request, HttpServletResponse response) {
        // Uwierzytelnienie użytkownika w Spring Security na podstawie loginu i hasła
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        // Pobranie danych profilowych użytkownika z bazy danych
        UserDetails user = userDetailsService.loadUserByUsername(request.username());
        // Generowanie krótkotrwałego Access Tokenu JWT
        String accessToken = jwtService.generateToken(user);
        
        // Generowanie długotrwałego Refresh Tokenu w bazie danych
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
        
        // Tworzenie ciasteczka HTTP-only przechowującego Refresh Token
        Cookie cookie = new Cookie("refreshToken", refreshToken.getToken());
        cookie.setHttpOnly(true); // Uniemożliwia odczytanie ciasteczka z poziomu JavaScript (ochrona przed XSS)
        cookie.setSecure(false); // Na środowisku lokalnym (HTTP) false, na produkcyjnym (HTTPS) powinno być true
        cookie.setPath("/"); // Zakres dostępności ciasteczka (cała aplikacja)
        cookie.setMaxAge(604800); // Czas życia ciasteczka w sekundach (7 dni)
        
        // Dodanie ciasteczka do nagłówków odpowiedzi HTTP
        response.addCookie(cookie);
        
        // Zwrócenie Access Tokenu w ciele JSON
        return new AuthResponse(accessToken);
    }

    // Metoda odświeżania sesji (POST /api/auth/refresh) odczytująca Refresh Token z ciasteczka HTTP-only
    @PostMapping("/refresh")
    public AuthResponse refresh(@CookieValue(name = "refreshToken", required = false) String refreshTokenStr) {
        // Sprawdzenie czy ciasteczko z Refresh Tokenem w ogóle zostało przesłane
        if (refreshTokenStr == null || refreshTokenStr.isBlank()) {
            throw new IllegalArgumentException("Brak tokenu odświeżania w ciasteczku. Zaloguj się ponownie.");
        }
        
        // Wyszukanie tokenu w bazie, weryfikacja jego ważności i wygenerowanie nowego Access Tokenu JWT
        return refreshTokenService.findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration) // Sprawdzenie czy token nie wygasł
                .map(RefreshToken::getUser) // Wyciągnięcie użytkownika powiązanego z tokenem
                .map(user -> {
                    String newAccessToken = jwtService.generateToken(user); // Wygenerowanie nowego Access Tokenu JWT
                    return new AuthResponse(newAccessToken); // Zwrócenie odpowiedzi
                })
                .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowy token odświeżania. Zaloguj się ponownie."));
    }

    // Metoda wylogowywania (POST /api/auth/logout) czyszcząca token w bazie oraz usuwająca ciasteczko
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // Pobranie aktualnego kontekstu uwierzytelnienia użytkownika
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Jeśli użytkownik jest poprawnie zalogowany
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            // Usunięcie Refresh Tokenu powiązanego z zalogowanym użytkownikiem z bazy danych
            refreshTokenService.deleteByUsername(authentication.getName());
        }
        
        // Tworzenie ciasteczka czyszczącego (nadpisanie pustą wartością i czasem życia 0 sekund)
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Instrukcja dla przeglądarki, aby natychmiast usunęła ciasteczko
        response.addCookie(cookie); // Dołączenie do odpowiedzi
        
        // Czyszczenie kontekstu bezpieczeństwa wątku
        SecurityContextHolder.clearContext();
        
        // Zwrócenie komunikatu o pomyślnym wylogowaniu
        return ResponseEntity.ok("Wylogowano pomyślnie.");
    }
}