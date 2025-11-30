package com.apiwosze.schooltrips.opiekun_wycieczki;

public record OpiekunWycieczkiDto(
        Rola rola,
        Long wycieczkaId,
        Long nauczycielId
) {
}
