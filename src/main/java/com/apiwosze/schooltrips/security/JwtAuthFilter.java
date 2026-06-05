package com.apiwosze.schooltrips.security; // Definicja pakietu bezpieczeństwa

import jakarta.servlet.FilterChain; // Import łańcucha filtrów servletów
import jakarta.servlet.ServletException; // Import wyjątków kontenera servletów
import jakarta.servlet.http.HttpServletRequest; // Import reprezentacji żądania HTTP
import jakarta.servlet.http.HttpServletResponse; // Import reprezentacji odpowiedzi HTTP
import org.springframework.beans.factory.annotation.Qualifier; // Import adnotacji do precyzowania wstrzykiwanego Beana o tej samej klasie/interfejsie
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Import klasy tokenu uwierzytelnienia w Spring Security
import org.springframework.security.core.context.SecurityContextHolder; // Import klasy przechowującej kontekst bezpieczeństwa (zalogowanego użytkownika)
import org.springframework.security.core.userdetails.UserDetails; // Import interfejsu danych użytkownika
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource; // Import źródła szczegółów żądania sieciowego (IP, sesja)
import org.springframework.stereotype.Component; // Import adnotacji Spring Component
import org.springframework.web.filter.OncePerRequestFilter; // Import klasy bazowej gwarantującej uruchomienie filtra dokładnie raz na żądanie HTTP
import org.springframework.web.servlet.HandlerExceptionResolver; // Import interfejsu do rozwiązywania wyjątków i przekierowania ich do handlera

import java.io.IOException; // Import wykluczeń wejścia/wyjścia

@Component // Rejestracja filtra jako komponentu w kontenerze Springa
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService; // Deklaracja serwisu obsługującego operacje na tokenach JWT
    private final CustomUserDetailsService userDetailsService; // Deklaracja serwisu wczytującego użytkowników z bazy danych
    private final HandlerExceptionResolver resolver; // Deklaracja resolvera wyjątków do obsługi błędów w filtrze (try-catch)

    // Konstruktor wstrzykujący zależności (resolver jest kwalifikowany nazwą 'handlerExceptionResolver' w celu uniknięcia konfliktów)
    public JwtAuthFilter(JwtService jwtService,
                         CustomUserDetailsService userDetailsService,
                         @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.resolver = resolver;
    }

    // Metoda wykonująca filtrowanie każdego przychodzącego żądania HTTP
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization"); // Pobranie wartości nagłówka "Authorization" z żądania HTTP
        final String jwt; // Zmienna przechowująca sam token JWT
        final String username; // Zmienna przechowująca login użytkownika wyciągnięty z tokenu

        // Jeśli nagłówek autoryzacji jest pusty lub nie zaczyna się od prefiksu "Bearer ", pomiń ten filtr i przejdź do następnego filtra w łańcuchu
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Przekazanie żądania dalej
            return; // Przerwanie dalszego wykonywania metody w tym filtrze
        }

        try {
            jwt = authHeader.substring(7); // Wycięcie tokenu JWT z nagłówka (pominięcie prefiksu "Bearer " czyli pierwszych 7 znaków)
            username = jwtService.extractUsername(jwt); // Wyciągnięcie nazwy użytkownika (username/subject) z tokenu za pomocą JwtService

            // Jeśli użytkownik został poprawnie odczytany i nie jest jeszcze zalogowany w obecnym kontekście bezpieczeństwa Spring Security
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Załadowanie szczegółów użytkownika (w tym ról i hasła) z bazy danych za pomocą CustomUserDetailsService
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Weryfikacja czy token jest poprawny (zgodność loginu i sprawdzenie daty ważności tokenu)
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Utworzenie obiektu uwierzytelnienia z danymi użytkownika i jego uprawnieniami (rolami)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    // Dołączenie dodatkowych szczegółów żądania sieciowego (np. adresu IP klienta)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Zapisanie uwierzytelnionego użytkownika w globalnym kontekście bezpieczeństwa wątku (Spring Security uznaje go za zalogowanego)
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response); // Przekazanie żądania do kolejnych filtrów w łańcuchu
        } catch (Exception ex) {
            // W razie jakiegokolwiek błędu (np. ExpiredJwtException, SignatureException), przekaż go do globalnego exception handlera
            // Zapobiega to zwróceniu przez Spring standardowej pustej strony błędu i umożliwia spójny zwrot JSON-a
            resolver.resolveException(request, response, null, ex);
        }
    }
}