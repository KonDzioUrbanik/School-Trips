package com.apiwosze.schooltrips.wycieczka;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record WycieczkaDto(
        @NotBlank(message = "Nazwa wycieczki nie może być pusta")
        String nazwa,
        @NotNull(message = "Data rozpoczęcia nie może być pusta")
        @FutureOrPresent(message = "Data rozpoczęcia musi być dzisiaj lub w przyszłości")
        LocalDate dataRozpoczecia,
        @NotNull(message = "Data zakończenia nie może być pusta")
        LocalDate dataZakonczenia,
        @NotBlank(message = "Miejsce docelowe nie może być puste")
        String miejsceDocelowe,
        @NotNull(message = "Koszt na osobę nie może być pusty")
        @DecimalMin(value = "0.0", inclusive = true, message = "Koszt na osobę nie może być ujemny")
        BigDecimal kosztNaOsobe,
        @NotNull(message = "Status wycieczki nie może być pusty")
        Status status,
        String planWycieczki
) {
}
