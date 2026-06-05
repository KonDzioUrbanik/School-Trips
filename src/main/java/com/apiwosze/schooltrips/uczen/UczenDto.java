package com.apiwosze.schooltrips.uczen;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record UczenDto(
        @NotBlank(message = "Imię ucznia nie może być puste")
        String imie,
        @NotBlank(message = "Nazwisko ucznia nie może być puste")
        String nazwisko,
        @NotNull(message = "Data urodzenia nie może być pusta")
        @Past(message = "Data urodzenia musi być z przeszłości")
        LocalDate data_urodzenia,
        @NotNull(message = "ID klasy nie może być puste")
        Long klasaId
) {
}
