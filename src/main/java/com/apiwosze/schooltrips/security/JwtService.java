package com.apiwosze.schooltrips.security; // Definicja pakietu bezpieczeństwa

import io.jsonwebtoken.Claims; // Import klasy Claims reprezentującej zawartość payloadu tokenu JWT
import io.jsonwebtoken.Jwts; // Import głównej klasy fasady biblioteki jjwt
import io.jsonwebtoken.SignatureAlgorithm; // Import algorytmów podpisywania tokenów JWT (np. HS256)
import io.jsonwebtoken.security.Keys; // Import klasy pomocniczej do tworzenia kluczy kryptograficznych
import org.springframework.beans.factory.annotation.Value; // Import adnotacji do wstrzykiwania wartości z konfiguracji
import org.springframework.security.core.GrantedAuthority; // Import reprezentacji przyznanych uprawnień/ról użytkownika
import org.springframework.security.core.userdetails.UserDetails; // Import interfejsu opisującego szczegóły użytkownika w Spring Security
import org.springframework.stereotype.Service; // Import adnotacji Spring Service oznaczającej klasę serwisową

import java.security.Key; // Import interfejsu klucza kryptograficznego
import java.util.Date; // Import klasy reprezentującej punkt w czasie (wymagany przez jjwt)
import java.util.HashMap; // Import mapy HashMap do claimsów
import java.util.Map; // Import interfejsu Map do przesyłania dodatkowych informacji w tokenie
import java.util.stream.Collectors; // Import kolektorów API strumieni do transformacji kolekcji ról

@Service // Rejestracja klasy jako serwisu w kontenerze Spring (automatycznie tworzony Singleton)
public class JwtService {
    private final Key key; // Zmienna przechowująca klucz symetryczny do podpisywania i weryfikacji tokenów JWT

    // Konstruktor pobierający sekret z właściwości 'jwt.secret' (np. ze zmiennej środowiskowej) lub stosujący bezpieczną wartość domyślną
    public JwtService(@Value("${jwt.secret:ToJestBardzoTajnyKluczDoGenerowaniaTokenowJWTKoregoNiktNieZna123!}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes()); // Wygenerowanie klucza kryptograficznego HMAC-SHA z bajtów sekretu
    }

    // Metoda generująca nowy token JWT na podstawie szczegółów użytkownika UserDetails
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>(); // Utworzenie pustej mapy na dodatkowe twierdzenia (claims) w tokenie
        
        // Wyciągnięcie ról/uprawnień użytkownika i przekształcenie ich w listę Stringów (np. ["ROLE_ADMIN"])
        var roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // Wyciągnięcie nazwy tekstowej uprawnienia
                .collect(Collectors.toList());      // Zgromadzenie ich do listy
        
        claims.put("roles", roles); // Dodanie ról do mapy claims pod kluczem "roles"
        return createToken(claims, userDetails.getUsername()); // Wywołanie metody tworzącej token z claimsami i nazwą użytkownika (subject)
    }

    // Prywatna metoda budująca token JWT przy użyciu biblioteki jjwt
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder() // Rozpoczęcie budowania tokenu JWT
                .setClaims(claims) // Ustawienie dodatkowych claimsów w payloadzie tokenu
                .setSubject(subject) // Ustawienie głównego tematu tokenu (loginu użytkownika)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Ustawienie daty wydania tokenu (bieżący moment)
                // Ustawienie daty wygaśnięcia tokenu na 10 godzin od teraz (10 * 60 * 60 * 1000 milisekund)
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(key, SignatureAlgorithm.HS256) // Podpisanie tokenu kluczem kryptograficznym przy użyciu algorytmu HS256
                .compact(); // Skompilowanie i zakodowanie tokenu do wynikowego formatu tekstowego (trzy części oddzielone kropkami)
    }

    // Metoda wyciągająca login użytkownika (subject) z tokenu JWT
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject(); // Pobranie pola "sub" (Subject) z rozszyfrowanych twierdzeń
    }

    // Metoda wyciągająca datę wygaśnięcia tokenu JWT
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration(); // Pobranie pola "exp" (Expiration) z rozszyfrowanych twierdzeń
    }

    // Prywatna metoda parsująca i weryfikująca podpis tokenu JWT oraz zwracająca pełen zestaw claimsów (twierdzeń)
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder() // Inicjalizacja parsera JWT
                .setSigningKey(key) // Ustawienie klucza symetrycznego do weryfikacji podpisu cyfrowego tokenu
                .build() // Zbudowanie parsera
                .parseClaimsJws(token) // Przeanalizowanie tokenu i sprawdzenie jego podpisu (rzuci wyjątek jeśli token jest zniekształcony/zmieniony)
                .getBody(); // Pobranie ciała (payloadu) tokenu zawierającego wszystkie claimsy
    }

    // Metoda sprawdzająca czy token wygasł (porównuje datę exp z czasem systemowym)
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date()); // Zwraca true jeśli data wygaśnięcia jest wcześniejsza niż obecny moment
    }

    // Metoda weryfikująca czy token jest poprawny dla danego użytkownika UserDetails
    public Boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token); // Wyciągnięcie loginu z tokenu
        // Token jest ważny jeśli login w tokenie zgadza się z loginem użytkownika oraz token jeszcze nie wygasł
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}