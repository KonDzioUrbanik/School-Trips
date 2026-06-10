package com.apiwosze.schooltrips.wycieczka;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO zwracający prognozę pogody dla danej wycieczki.
 * Pola nazwane zgodnie z tym czego oczekuje frontend (app.js).
 */
public record WeatherDto(
        String locationName,   // Nazwa miejsca (używana przez JS jako data.locationName)
        String source,         // Źródło danych (Open-Meteo forecast / archive)
        List<DailyWeather> days // Lista dni pogodowych (używana przez JS jako data.days)
) {
    public record DailyWeather(
            LocalDate date,       // Data jako YYYY-MM-DD (parsowana przez new Date() w JS)
            Double tempMax,       // Temperatura maksymalna (°C)
            Double tempMin,       // Temperatura minimalna (°C)
            Integer weatherCode,  // Kod WMO pogody (https://open-meteo.com/en/docs)
            String description    // Czytelny opis np. "Czyste niebo ☀️"
    ) {}
}
