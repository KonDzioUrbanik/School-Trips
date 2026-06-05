package com.apiwosze.schooltrips.security; // Definicja pakietu dla klas związanych z zabezpieczeniami i autoryzacją

import com.apiwosze.schooltrips.exception.ErrorResponse; // Import klasy reprezentującej odpowiedź o błędzie
import com.fasterxml.jackson.databind.ObjectMapper; // Import klasy Jackson służącej do serializacji obiektów Java do formatu JSON
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Import modułu Jackson do obsługi nowych klas czasu Javy (np. LocalDateTime)
import jakarta.servlet.ServletException; // Import podstawowego wyjątku kontenera servletów
import jakarta.servlet.http.HttpServletRequest; // Import reprezentacji żądania HTTP
import jakarta.servlet.http.HttpServletResponse; // Import reprezentacji odpowiedzi HTTP do modyfikacji statusu i strumienia wyjściowego
import org.springframework.http.HttpStatus; // Import enuma reprezentującego statusy HTTP
import org.springframework.security.core.AuthenticationException; // Import wyjątku rzucanego przez Spring Security przy braku uwierzytelnienia
import org.springframework.security.web.AuthenticationEntryPoint; // Import interfejsu punktu wejścia dla żądań nieautoryzowanych
import org.springframework.stereotype.Component; // Import adnotacji oznaczającej klasę jako Bean zarządzany przez Springa

import java.io.IOException; // Import wyjątku wejścia/wyjścia (operacje na strumieniu odpowiedzi)

@Component // Rejestracja tej klasy jako komponentu Springa w celu wstrzykiwania do konfiguracji
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Metoda commence wywołuje się automatycznie, gdy użytkownik próbuje uzyskać dostęp do chronionego zasobu bez ważnego tokenu JWT
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Ustawienie nagłówka Content-Type odpowiedzi na application/json ze standardowym kodowaniem UTF-8
        response.setContentType("application/json;charset=UTF-8");
        // Ustawienie kodu statusu HTTP odpowiedzi na 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Utworzenie spójnego obiektu odpowiedzi o błędzie z opisem po polsku
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(), // Kod statusu 401
                "Unauthorized",                 // Błąd
                "Brak autoryzacji lub nieprawidłowy token JWT. Zaloguj się, aby uzyskać dostęp.", // Wiadomość wyjaśniająca
                request.getRequestURI()         // Ścieżka, pod którą odmówiono dostępu
        );

        // Instancjonowanie parsera JSON
        ObjectMapper objectMapper = new ObjectMapper();
        // Rejestracja modułu umożliwiającego poprawną konwersję LocalDateTime do formatu JSON (pola timestamp)
        objectMapper.registerModule(new JavaTimeModule());
        // Zapisanie obiektu ErrorResponse jako stringu JSON bezpośrednio do strumienia wyjściowego odpowiedzi HTTP
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
