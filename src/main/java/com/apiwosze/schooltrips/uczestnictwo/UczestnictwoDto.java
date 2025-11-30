package com.apiwosze.schooltrips.uczestnictwo;

public record UczestnictwoDto(
        Long uczenId,
        Long wycieczkaId,
        boolean czyJedzie,
        String uwagi
) {
}
