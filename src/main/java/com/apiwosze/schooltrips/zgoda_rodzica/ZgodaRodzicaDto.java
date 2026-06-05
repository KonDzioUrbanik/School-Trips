package com.apiwosze.schooltrips.zgoda_rodzica;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ZgodaRodzicaDto(
        @NotNull(message = "ID uczestnictwa nie może być puste")
        Long uczestnictwoId,
        @NotNull(message = "Data podpisania nie może być pusta")
        LocalDate dataPodpisania,
        @NotNull(message = "Forma wyrażenia zgody nie może być pusta")
        Forma forma,
        boolean czyDostarczona
) {
}
