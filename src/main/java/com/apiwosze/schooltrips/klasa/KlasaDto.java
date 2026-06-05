package com.apiwosze.schooltrips.klasa; // Definicja pakietu dla modułu Klasy

import jakarta.validation.constraints.NotBlank; // Import adnotacji sprawdzającej czy tekst nie jest pusty

// Rekord reprezentujący dane wejściowe klasy (DTO - Data Transfer Object) przesyłane w żądaniach API
public record KlasaDto(
        @NotBlank(message = "Nazwa klasy nie może być pusta") // Walidacja: nazwa nie może składać się z samych białych znaków ani być nullem
        String nazwa, // Nazwa klasy (np. "1A", "3C")

        @NotBlank(message = "Profil klasy nie może być pusty") // Walidacja: profil nie może być pusty
        String profil // Profil klasy (np. "matematyczno-fizyczny", "biologiczno-chemiczny")
) {
}
