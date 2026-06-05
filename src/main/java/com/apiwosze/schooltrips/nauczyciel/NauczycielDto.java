package com.apiwosze.schooltrips.nauczyciel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record NauczycielDto(
        @NotBlank(message = "Imię nauczyciela nie może być puste")
        String imie,
        @NotBlank(message = "Nazwisko nauczyciela nie może być puste")
        String nazwisko,
        @NotBlank(message = "Przedmiot nie może być pusty")
        String przedmiot,
        @NotBlank(message = "Telefon kontaktowy nie może być pusty")
        @Pattern(regexp = "^\\+?[0-9\\s-]{9,15}$", message = "Nieprawidłowy format numeru telefonu")
        String telefon_kontaktowy
) {
}
