package com.apiwosze.schooltrips.security; // Definicja pakietu dla klas bezpieczeństwa

import com.apiwosze.schooltrips.exception.ErrorResponse; // Import klasy reprezentującej odpowiedź o błędzie
import com.fasterxml.jackson.databind.ObjectMapper; // Import klasy Jackson służącej do serializacji obiektów do JSON
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Import modułu Jackson do poprawnej obsługi LocalDateTime
import jakarta.servlet.ServletException; // Import wyjątku servletu
import jakarta.servlet.http.HttpServletRequest; // Import reprezentacji żądania HTTP
import jakarta.servlet.http.HttpServletResponse; // Import reprezentacji odpowiedzi HTTP
import org.springframework.http.HttpStatus; // Import statusów HTTP
import org.springframework.security.access.AccessDeniedException; // Import wyjątku braku wystarczających ról
import org.springframework.security.web.access.AccessDeniedHandler; // Import interfejsu obsługi odmowy dostępu
import org.springframework.stereotype.Component; // Import adnotacji Spring Component

import java.io.IOException; // Import wyjątków wejścia/wyjścia

@Component // Rejestracja klasy jako komponentu w kontenerze Springa
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    // Metoda handle wywołuje się automatycznie, gdy uwierzytelniony użytkownik próbuje wejść na zasób, do którego nie ma roli (np. uczeń chce usunąć klasę)
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // Ustawienie nagłówka odpowiedzi na format JSON z kodowaniem UTF-8
        response.setContentType("application/json;charset=UTF-8");
        // Ustawienie statusu HTTP odpowiedzi na 403 Forbidden
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // Utworzenie standardowego obiektu błędu 403 z polskim opisem
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(), // Kod statusu 403
                "Forbidden",                  // Nazwa błędu
                "Brak wystarczających uprawnień do wykonania tej operacji.", // Wiadomość odmowy
                request.getRequestURI()       // Próbowana ścieżka URI
        );

        // Instancjonowanie serializatora JSON
        ObjectMapper objectMapper = new ObjectMapper();
        // Zapewnienie poprawnej obsługi LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());
        // Zapisanie wygenerowanego JSON-a ze szczegółami odmowy do ciała odpowiedzi
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
