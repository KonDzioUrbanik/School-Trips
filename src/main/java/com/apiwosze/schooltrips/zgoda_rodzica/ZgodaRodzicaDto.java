package com.apiwosze.schooltrips.zgoda_rodzica;

import java.time.LocalDate;

public record ZgodaRodzicaDto(
        Long uczestnictwoId,
        LocalDate dataPodpisania,
        Forma forma,
        boolean czyDostarczona
) {
}
