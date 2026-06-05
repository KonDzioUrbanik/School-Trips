package com.apiwosze.schooltrips.security; // Definicja pakietu bezpieczeństwa

import jakarta.validation.Valid; // Import adnotacji do uruchamiania walidacji obiektów
import jakarta.validation.constraints.NotBlank; // Import adnotacji wymuszającej niepuste pole tekstowe
import org.springframework.security.authentication.AuthenticationManager; // Import menedżera uwierzytelniania Spring Security
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Import reprezentacji tokenu uwierzytelniania hasłem i loginem
import org.springframework.security.core.userdetails.UserDetails; // Import interfejsu danych użytkownika
import org.springframework.web.bind.annotation.*; // Import adnotacji Spring Web do obsługi żądań REST

@RestController // Oznacza klasę jako kontroler REST (zwracane obiekty są automatycznie serializowane do formatu JSON)
@RequestMapping("/api/auth") // Bazowa ścieżka dla wszystkich endpointów w tym kontrolerze (np. /api/auth/login)
public class AuthController {

    private final AuthenticationManager authenticationManager; // Deklaracja menedżera uwierzytelniania
    private final CustomUserDetailsService userDetailsService; // Deklaracja serwisu wczytującego szczegóły użytkownika
    private final JwtService jwtService; // Deklaracja serwisu do generowania tokenów JWT

    // Konstruktor wstrzykujący wymagane zależności
    public AuthController(AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    // Rekord reprezentujący dane wejściowe żądania logowania (z walidacją braku pustych pól)
    public record AuthRequest(
            @NotBlank(message = "Nazwa użytkownika nie może być pusta")
            String username, // Pole loginu użytkownika
            @NotBlank(message = "Hasło nie może być puste")
            String password  // Pole hasła użytkownika
    ) {}

    // Rekord reprezentujący strukturę odpowiedzi z wygenerowanym tokenem JWT
    public record AuthResponse(String token) {}

    // Endpoint obsługujący żądanie logowania (metoda POST pod adresem /api/auth/login)
    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid AuthRequest request) {
        // Próba uwierzytelnienia użytkownika za pomocą loginu i hasła. 
        // W razie błędnych danych, rzucany jest wyjątek BadCredentialsException przechwytywany przez GlobalExceptionHandler
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        // Pobranie pełnych danych uwierzytelnionego użytkownika z bazy
        UserDetails user = userDetailsService.loadUserByUsername(request.username());
        // Wygenerowanie bezpiecznego tokenu JWT podpisanego kluczem serwera
        String token = jwtService.generateToken(user);
        // Zwrócenie tokenu zapakowanego w strukturę JSON (AuthResponse)
        return new AuthResponse(token);
    }
}