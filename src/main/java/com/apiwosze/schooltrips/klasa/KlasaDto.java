package com.apiwosze.schooltrips.klasa;

import jakarta.validation.constraints.NotBlank;

public record KlasaDto(
        @NotBlank(message = "Nazwa klasy nie może być pusta")
        String nazwa,
        @NotBlank(message = "Profil klasy nie może być pusty")
        String profil
) {
}
