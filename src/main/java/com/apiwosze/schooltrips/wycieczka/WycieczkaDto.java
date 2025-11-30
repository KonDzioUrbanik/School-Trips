package com.apiwosze.schooltrips.wycieczka;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WycieczkaDto(
        String nazwa,
        LocalDate dataRozpoczecia,
        LocalDate dataZakonczenia,
        String miejsceDocelowe,
        BigDecimal kosztNaOsobe,
        Status status
) {
}
