package com.apiwosze.schooltrips.uczestnictwo;

import jakarta.validation.constraints.NotNull;

public record UczestnictwoDto(
        @NotNull(message = "ID ucznia nie może być puste")
        Long uczenId,
        @NotNull(message = "ID wycieczki nie może być puste")
        Long wycieczkaId,
        boolean czyJedzie,
        String uwagi
) {
}
