package com.apiwosze.schooltrips.exception; // Definicja pakietu dla klas obsługi błędów i wyjątków

import io.jsonwebtoken.JwtException; // Import klasy bazowej wyjątków biblioteki JWT
import jakarta.persistence.EntityNotFoundException; // Import wyjątku rzucanego przez JPA gdy encja nie zostanie znaleziona
import jakarta.servlet.http.HttpServletRequest; // Import reprezentacji żądania HTTP do pobierania adresów URI
import jakarta.validation.ConstraintViolationException; // Import wyjątku walidacji parametrów metod i ograniczeń bazy danych
import org.slf4j.Logger; // Import interfejsu logera do rejestrowania zdarzeń i błędów
import org.slf4j.LoggerFactory; // Import fabryki logerów SLF4J
import org.springframework.dao.DataIntegrityViolationException; // Import wyjątku bazy danych przy naruszeniu spójności (np. duplikaty)
import org.springframework.http.HttpStatus; // Import enuma reprezentującego statusy odpowiedzi HTTP
import org.springframework.http.ResponseEntity; // Import klasy reprezentującej całą odpowiedź HTTP wraz ze statusem i nagłówkami
import org.springframework.security.access.AccessDeniedException; // Import wyjątku braku uprawnień (Spring Security)
import org.springframework.security.authentication.BadCredentialsException; // Import wyjątku błędnego logowania (Spring Security)
import org.springframework.validation.FieldError; // Import klasy przechowującej szczegóły błędu walidacji konkretnego pola
import org.springframework.web.bind.MethodArgumentNotValidException; // Import wyjątku walidacji ciał żądań (@Valid @RequestBody)
import org.springframework.web.bind.annotation.ExceptionHandler; // Import adnotacji oznaczającej metodę jako obsługującą dany wyjątek
import org.springframework.web.bind.annotation.RestControllerAdvice; // Import adnotacji czyniącej klasę globalnym kontrolerem obsługi wyjątków REST

import java.util.HashMap; // Import implementacji mapy HashMap
import java.util.Map; // Import interfejsu Map do przesyłania struktury klucz-wartość
import java.util.NoSuchElementException; // Import standardowego wyjątku braku elementu w kolekcji/Optionalu

@RestControllerAdvice // Klasa nasłuchuje na wyjątki rzucane przez wszystkie kontrolery w aplikacji
public class GlobalExceptionHandler {

    // Tworzenie logera powiązanego z tą klasą do zapisywania logów na serwerze
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. Metoda obsługująca błędy walidacji parametrów wejściowych przesłanych w JSON-ie (@Valid na @RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        Map<String, String> errors = new HashMap<>(); // Mapa przechowująca pary: błędne pole -> opis błędu
        
        // Iteracja po wszystkich błędach walidacji wykrytych przez Spring
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField(); // Pobranie nazwy pola, które nie przeszło walidacji
            String errorMessage = error.getDefaultMessage(); // Pobranie ustawionej wiadomości o błędzie
            errors.put(fieldName, errorMessage); // Umieszczenie pary w mapie błędów
        });

        // Konstruowanie ustrukturyzowanej odpowiedzi o błędzie (status 400 Bad Request)
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), // Kod liczbowy 400
                "Bad Request", // Tekst statusu
                "Błąd walidacji danych wejściowych.", // Ogólny komunikat dla klienta
                request.getRequestURI(), // Pobranie ścieżki URI żądania
                errors // Załączenie mapy ze szczegółowymi błędami pól
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse); // Zwrócenie odpowiedzi z kodem 400
    }

    // 2. Metoda obsługująca błędy naruszenia ograniczeń walidacyjnych na poziomie pojedynczych zmiennych (np. parametrów URL)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        Map<String, String> errors = new HashMap<>(); // Utworzenie mapy na błędy pól
        
        // Iteracja po naruszeniach ograniczeń walidacji
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString(); // Pobranie ścieżki do właściwości (np. createUczen.uczenDto.imie)
            errors.put(propertyPath, violation.getMessage()); // Dodanie informacji do mapy
        });

        // Konstruowanie ustrukturyzowanej odpowiedzi o błędzie (status 400)
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), // Kod statusu 400
                "Bad Request",
                "Błąd walidacji ograniczeń.",
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse); // Zwrócenie odpowiedzi z kodem 400
    }

    // 3. Metoda obsługująca błędy logiczne i walidacje wewnątrz-serwisowe (np. rzucenie IllegalArgumentException)
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleBusinessExceptions(
            RuntimeException ex, HttpServletRequest request) {
        
        // Konstruowanie odpowiedzi o błędzie na podstawie komunikatu z rzuconego wyjątku (status 400)
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), // Kod statusu 400
                "Bad Request",
                ex.getMessage(), // Dynamiczny komunikat przekazany z logiki biznesowej
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse); // Zwrócenie odpowiedzi z kodem 400
    }

    // 4. Metoda obsługująca błędy braku uprawnień - np. gdy zalogowany nauczyciel próbuje wejść na endpoint administratora
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        
        // Konstruowanie ustrukturyzowanego błędu 403 Forbidden
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(), // Kod statusu 403
                "Forbidden",
                "Brak wystarczających uprawnień do wykonania tej operacji.", // Bezpieczny, czytelny komunikat
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse); // Zwrócenie odpowiedzi z kodem 403
    }

    // 5. Metoda obsługująca błędy uwierzytelniania - np. podanie niepoprawnego hasła przy logowaniu
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        
        // Konstruowanie ustrukturyzowanego błędu 401 Unauthorized
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(), // Kod statusu 401
                "Unauthorized",
                "Niepoprawny login lub hasło.", // Standardowy komunikat zabezpieczający przed ujawnieniem, czy login istnieje
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse); // Zwrócenie odpowiedzi z kodem 401
    }

    // 6. Metoda obsługująca błędy weryfikacji tokenu JWT (np. wygasły token lub zniekształcony podpis)
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(
            JwtException ex, HttpServletRequest request) {
        
        // Konstruowanie ustrukturyzowanego błędu 401 Unauthorized dla problemów z tokenem
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(), // Kod statusu 401
                "Unauthorized",
                "Problem z autoryzacją: " + ex.getMessage(), // Szczegółowe wyjaśnienie (np. JWT expired)
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse); // Zwrócenie odpowiedzi z kodem 401
    }

    // 7. Metoda obsługująca błędy nieznalezienia zasobu w bazie (np. findById(id).orElseThrow())
    @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(
            Exception ex, HttpServletRequest request) {
        
        // Konstruowanie ustrukturyzowanego błędu 404 Not Found
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(), // Kod statusu 404
                "Not Found",
                "Szukany zasób nie istnieje.", // Przyjazny komunikat o braku rekordu
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse); // Zwrócenie odpowiedzi z kodem 404
    }

    // 8. Metoda obsługująca błędy bazy danych - np. próba dodania klasy o nazwie, która już istnieje (klucz unikalny)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        // Zalogowanie ostrzeżenia na konsoli serwera dla programisty
        log.warn("Naruszenie więzów spójności bazy danych: {}", ex.getMessage());

        // Konstruowanie ustrukturyzowanego błędu 409 Conflict
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(), // Kod statusu 409
                "Conflict",
                "Wystąpił konflikt danych w bazie. Upewnij się, że przesyłane identyfikatory są poprawne i nie są powtórzone.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse); // Zwrócenie odpowiedzi z kodem 409
    }

    // 9. Rezerwowy handler dla wszystkich nieprzewidzianych i nieobsłużonych wyjątków serwera (np. NullPointerException)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            Exception ex, HttpServletRequest request) {
        
        // Zalogowanie pełnego śladu stosu (stacktrace) błędu w celu debugowania na serwerze
        log.error("Nieoczekiwany błąd w aplikacji: ", ex);

        // Konstruowanie ustrukturyzowanego błędu 500 Internal Server Error
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), // Kod statusu 500
                "Internal Server Error",
                "Wystąpił nieoczekiwany błąd serwera. Skontaktuj się z administratorem.", // Bezpieczny komunikat ukrywający szczegóły implementacji
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // Zwrócenie odpowiedzi z kodem 500
    }
}
