package com.apiwosze.schooltrips.exception; // Definicja pakietu dla wyjątków i błędów

import java.time.LocalDateTime; // Import klasy do obsługi daty i czasu
import java.util.Map; // Import interfejsu Map do obsługi szczegółów błędów

// Rekord w Javie służący do przesyłania ustrukturyzowanych informacji o błędach (Immutable DTO)
public record ErrorResponse(
        LocalDateTime timestamp, // Dokładna data i godzina wystąpienia błędu
        int status,              // Kod statusu HTTP (np. 400, 401, 403, 404, 500)
        String error,            // Nazwa statusu HTTP (np. "Bad Request", "Internal Server Error")
        String message,          // Przyjazny komunikat tekstowy wyjaśniający przyczynę błędu
        String path,             // Ścieżka URI żądania, które wywołało błąd (np. /api/wycieczka)
        Map<String, String> details // Opcjonalna mapa ze szczegółami błędów (np. nazwa pola -> błąd walidacji)
) {
    // Konstruktor pomocniczy dla błędów bez dodatkowych szczegółów (automatycznie ustawia aktualny czas)
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null); // Wywołanie głównego konstruktora z wartościami domyślnymi
    }

    // Konstruktor pomocniczy dla błędów walidacji zawierający mapę szczegółów
    public ErrorResponse(int status, String error, String message, String path, Map<String, String> details) {
        this(LocalDateTime.now(), status, error, message, path, details); // Wywołanie głównego konstruktora z mapą szczegółów
    }
}
