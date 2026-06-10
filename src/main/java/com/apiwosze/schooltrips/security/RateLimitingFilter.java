package com.apiwosze.schooltrips.security; // Definicja pakietu bezpieczeństwa

import com.apiwosze.schooltrips.exception.ErrorResponse; // Import struktury błędu
import com.fasterxml.jackson.databind.ObjectMapper; // Import parsera JSON Jackson
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Import modułu czasu Jackson
import io.github.bucket4j.Bandwidth; // Import klasy opisującej ograniczenia przepustowości
import io.github.bucket4j.Bucket; // Import klasy wiadra tokenów (Token Bucket)
import io.github.bucket4j.Refill; // Import klasy określającej parametry uzupełniania tokenów
import jakarta.servlet.*; // Import interfejsów specyfikacji Servletów
import jakarta.servlet.http.HttpServletRequest; // Import żądania HTTP
import jakarta.servlet.http.HttpServletResponse; // Import odpowiedzi HTTP
import org.springframework.http.HttpStatus; // Import statusów HTTP
import org.springframework.stereotype.Component; // Import adnotacji Spring Component

import java.io.IOException; // Import wyjątków wejścia/wyjścia
import java.time.Duration; // Import klasy okresu czasu Javy
import java.util.Map; // Import interfejsu Map
import java.util.concurrent.ConcurrentHashMap; // Import bezpiecznej wielowątkowo implementacji mapy

@Component // Rejestracja filtra jako komponentu zarządzanego przez Springa
public class RateLimitingFilter implements Filter {

    // Klienci identyfikowani są po adresie IP. ConcurrentHashMap zapewnia bezpieczny dostęp w środowisku wielowątkowym
    // Cache przechowujący wiadra dla adresów IP dla logowania (login brute-force protect)
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    
    // Cache przechowujący wiadra dla adresów IP dla pozostałych zapytań do API (DDoS protection)
    private final Map<String, Bucket> apiBuckets = new ConcurrentHashMap<>();

    // Definiowanie limitów dla logowania: maksymalnie 30 prób na minutę, uzupełniane co minutę
    private Bucket createLoginBucket() {
        return Bucket.builder()
                // Limit: pojemność 30 tokenów, uzupełniane w ilości 30 tokenów co 1 minutę
                .addLimit(Bandwidth.classic(30, Refill.intervally(30, Duration.ofMinutes(1))))
                .build();
    }

    // Definiowanie limitów dla pozostałych zapytań API: maksymalnie 500 zapytań na minutę
    private Bucket createApiBucket() {
        return Bucket.builder()
                // Limit: pojemność 500 tokenów, uzupełniane w ilości 500 tokenów co 1 minutę
                .addLimit(Bandwidth.classic(500, Refill.intervally(500, Duration.ofMinutes(1))))
                .build();
    }

    // Metoda filtrująca wywoływana przy każdym żądaniu HTTP przed wejściem do Spring Security i kontrolerów
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String ip = httpRequest.getRemoteAddr(); // Pobranie adresu IP klienta
        String path = httpRequest.getRequestURI(); // Pobranie ścieżki żądania (URI)
        
        // Pomiń limitowanie dla zasobów statycznych i stron (limitujemy tylko rzeczywiste API)
        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }
        
        Bucket bucket;
        // Rozróżnienie limitów na podstawie ścieżki żądania
        if (path.equals("/api/auth/login")) {
            // Dla logowania pobieramy/tworzymy wiadro z mapy loginBuckets
            bucket = loginBuckets.computeIfAbsent(ip, k -> createLoginBucket());
        } else {
            // Dla wszystkich innych adresów pobieramy/tworzymy wiadro z mapy apiBuckets
            bucket = apiBuckets.computeIfAbsent(ip, k -> createApiBucket());
        }

        // Spróbuj pobrać 1 token z wiadra klienta
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response); // Jeśli się udało (limit nieprzekroczony), przekaż żądanie dalej
        } else {
            // Jeśli zabrakło tokenów (przekroczono limit żądań)
            // Ustawienie statusu HTTP 429 Too Many Requests
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json;charset=UTF-8");

            // Konstruowanie spójnej odpowiedzi JSON o błędzie
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    "Too Many Requests",
                    "Przekroczono limit zapytań. Spróbuj ponownie za chwilę.",
                    path
            );

            // Zapisanie wygenerowanego JSON-a bezpośrednio do strumienia odpowiedzi
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            httpResponse.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
}
