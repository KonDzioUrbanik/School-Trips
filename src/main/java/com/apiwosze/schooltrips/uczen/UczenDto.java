package com.apiwosze.schooltrips.uczen;

import java.time.LocalDate;

public record UczenDto(
        String imie,
        String nazwisko,
        LocalDate data_urodzenia,
        Long klasaId
) {
}
