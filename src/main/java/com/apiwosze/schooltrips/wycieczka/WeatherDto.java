package com.apiwosze.schooltrips.wycieczka;

import java.time.LocalDate;
import java.util.List;

public record WeatherDto(
        String miejsce,
        String source,
        List<DailyWeather> daily
) {
    public record DailyWeather(
            LocalDate date,
            Double tempMax,
            Double tempMin,
            Integer weatherCode,
            String description
    ) {}
}
