package com.apiwosze.schooltrips.nauczyciel; // Definicja pakietu dla modułu Nauczycieli

import jakarta.validation.constraints.NotBlank; // Import walidatora wymuszającego niepuste pole
import jakarta.validation.constraints.Pattern; // Import walidatora sprawdzającego dopasowanie do wzorca (regex)

// Rekord reprezentujący dane przesyłane w żądaniach API dotyczące nauczyciela
public record NauczycielDto(
        @NotBlank(message = "Imię nauczyciela nie może być puste") // Walidacja: imię nie może być puste
        String imie, // Imię nauczyciela

        @NotBlank(message = "Nazwisko nauczyciela nie może być puste") // Walidacja: nazwisko nie może być puste
        String nazwisko, // Nazwisko nauczyciela

        @NotBlank(message = "Przedmiot nie może być pusty") // Walidacja: nauczany przedmiot nie może być pusty
        String przedmiot, // Nauczany przedmiot (np. "Matematyka", "Geografia")

        @NotBlank(message = "Telefon kontaktowy nie może być pusty") // Walidacja: telefon nie może być pusty
        @Pattern(regexp = "^\\+?[0-9\\s-]{9,15}$", message = "Nieprawidłowy format numeru telefonu") // Sprawdzenie poprawności zapisu telefonu (od 9 do 15 cyfr, opcjonalnie spacja, kreska lub +)
        String telefon_kontaktowy // Telefon kontaktowy nauczyciela
) {
}
