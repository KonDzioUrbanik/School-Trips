package com.apiwosze.schooltrips.opiekun_wycieczki;

import jakarta.validation.constraints.NotNull;

public record OpiekunWycieczkiDto(
        @NotNull(message = "Rola opiekuna nie może być pusta")
        Rola rola,
        @NotNull(message = "ID wycieczki nie może być puste")
        Long wycieczkaId,
        @NotNull(message = "ID nauczyciela nie może być puste")
        Long nauczycielId
) {
}
