package com.apiwosze.schooltrips.wycieczka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeatherService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherDto getWeatherForTrip(WycieczkaModel wycieczka) {
        String city = wycieczka.getMiejsce_docelowe();
        LocalDate start = wycieczka.getData_rozpoczecia();
        LocalDate end = wycieczka.getData_zakonczenia();

        if (city == null || city.trim().isEmpty() || start == null || end == null) {
            throw new IllegalArgumentException("Niepełne dane wycieczki (brak celu lub dat)!");
        }

        try {
            // Krok 1: Geokodowanie lokalizacji na współrzędne
            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" 
                    + URLEncoder.encode(city.trim(), StandardCharsets.UTF_8) 
                    + "&count=1&language=pl";

            HttpRequest geoRequest = HttpRequest.newBuilder()
                    .uri(URI.create(geoUrl))
                    .timeout(java.time.Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> geoResponse = httpClient.send(geoRequest, HttpResponse.BodyHandlers.ofString());

            if (geoResponse.statusCode() != 200) {
                throw new IllegalStateException("Nie udało się pobrać współrzędnych z API geolokalizacji. Kod statusu: " + geoResponse.statusCode());
            }

            JsonNode geoRoot = objectMapper.readTree(geoResponse.body());
            JsonNode results = geoRoot.path("results");
            if (results.isMissingNode() || !results.isArray() || results.size() == 0) {
                throw new IllegalArgumentException("Nie znaleziono lokalizacji geograficznej dla miejsca: " + city);
            }

            JsonNode firstResult = results.get(0);
            double lat = firstResult.path("latitude").asDouble();
            double lon = firstResult.path("longitude").asDouble();
            String resolvedCity = firstResult.path("name").asText(city);

            // Krok 2: Ustalenie właściwego API pogodowego (Forecast vs Archive)
            LocalDate today = LocalDate.now();
            LocalDate maxForecastDate = today.plusDays(16);

            String weatherUrl;
            String sourceDescription;
            boolean isShifted = false;

            LocalDate queryStart = start;
            LocalDate queryEnd = end;

            if (start.isBefore(today)) {
                if (end.isBefore(today)) {
                    // Wycieczka w przeszłości
                    weatherUrl = String.format("https://archive-api.open-meteo.com/v1/archive?latitude=%f&longitude=%f&start_date=%s&end_date=%s&daily=temperature_2m_max,temperature_2m_min,weathercode&timezone=auto",
                            lat, lon, start, end);
                    sourceDescription = "Historyczne dane pogodowe (Open-Meteo Archiwum)";
                } else {
                    // Wycieczka rozpoczęła się w przeszłości, a kończy w przyszłości - przesunięcie o rok wstecz
                    queryStart = start.minusYears(1);
                    queryEnd = end.minusYears(1);
                    isShifted = true;
                    weatherUrl = String.format("https://archive-api.open-meteo.com/v1/archive?latitude=%f&longitude=%f&start_date=%s&end_date=%s&daily=temperature_2m_max,temperature_2m_min,weathercode&timezone=auto",
                            lat, lon, queryStart, queryEnd);
                    sourceDescription = "Szacunkowa prognoza (dane historyczne z zeszłego roku)";
                }
            } else {
                // Wycieczka w przyszłości
                if (end.isBefore(maxForecastDate)) {
                    // Całość mieści się w standardowej prognozie 16-dniowej
                    weatherUrl = String.format("https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&start_date=%s&end_date=%s&daily=temperature_2m_max,temperature_2m_min,weathercode&timezone=auto",
                            lat, lon, start, end);
                    sourceDescription = "Bieżąca prognoza pogody (Open-Meteo)";
                } else {
                    // Wycieczka zbyt daleko w przyszłość - pobieramy dane z zeszłego roku jako estymację
                    queryStart = start.minusYears(1);
                    queryEnd = end.minusYears(1);
                    isShifted = true;
                    weatherUrl = String.format("https://archive-api.open-meteo.com/v1/archive?latitude=%f&longitude=%f&start_date=%s&end_date=%s&daily=temperature_2m_max,temperature_2m_min,weathercode&timezone=auto",
                            lat, lon, queryStart, queryEnd);
                    sourceDescription = "Szacunkowa prognoza (dane historyczne z zeszłego roku)";
                }
            }

            HttpRequest weatherRequest = HttpRequest.newBuilder()
                    .uri(URI.create(weatherUrl))
                    .timeout(java.time.Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> weatherResponse = httpClient.send(weatherRequest, HttpResponse.BodyHandlers.ofString());

            if (weatherResponse.statusCode() != 200) {
                throw new IllegalStateException("Nie udało się pobrać danych pogodowych. Kod statusu: " + weatherResponse.statusCode());
            }

            JsonNode weatherRoot = objectMapper.readTree(weatherResponse.body());
            JsonNode dailyNode = weatherRoot.path("daily");

            if (dailyNode.isMissingNode()) {
                throw new IllegalStateException("API pogodowe nie zwróciło danych dziennych.");
            }

            JsonNode times = dailyNode.path("time");
            JsonNode tempsMax = dailyNode.path("temperature_2m_max");
            JsonNode tempsMin = dailyNode.path("temperature_2m_min");
            JsonNode weatherCodes = dailyNode.path("weathercode");

            List<WeatherDto.DailyWeather> dailyList = new ArrayList<>();
            int dataSize = times.size();

            for (int i = 0; i < dataSize; i++) {
                LocalDate date;
                if (isShifted) {
                    // Mapowanie daty historycznej z powrotem na właściwą datę wycieczki
                    LocalDate histDate = LocalDate.parse(times.get(i).asText());
                    date = histDate.plusYears(1);
                } else {
                    date = LocalDate.parse(times.get(i).asText());
                }

                double maxT = tempsMax.get(i).asDouble();
                double minT = tempsMin.get(i).asDouble();
                int code = weatherCodes.get(i).asInt();
                String desc = getWeatherDescription(code);

                dailyList.add(new WeatherDto.DailyWeather(date, maxT, minT, code, desc));
            }

            return new WeatherDto(resolvedCity, sourceDescription, dailyList);

        } catch (Exception e) {
            System.err.println("Błąd podczas pobierania pogody dla wycieczki: " + e.getMessage());
            return getFallbackWeather(city, start, end, e.getMessage());
        }
    }

    private WeatherDto getFallbackWeather(String city, LocalDate start, LocalDate end, String errorMessage) {
        List<WeatherDto.DailyWeather> dailyList = new ArrayList<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            dailyList.add(new WeatherDto.DailyWeather(
                    current,
                    20.0,
                    12.0,
                    2,
                    "Częściowe zachmurzenie (Brak połączenia z API) ⛅"
            ));
            current = current.plusDays(1);
        }
        return new WeatherDto(
                city,
                "Prognoza domyślna (Błąd pobierania: " + errorMessage + ")",
                dailyList
        );
    }

    public static String getWeatherDescription(int code) {
        return switch (code) {
            case 0 -> "Czyste niebo ☀️";
            case 1 -> "Głównie czyste niebo 🌤️";
            case 2 -> "Częściowe zachmurzenie ⛅";
            case 3 -> "Zachmurzenie całkowite ☁️";
            case 45, 48 -> "Mgła 🌫️";
            case 51, 53, 55 -> "Mżawka 🌦️";
            case 56, 57 -> "Marznąca mżawka 🌧️❄️";
            case 61, 63, 65 -> "Deszcz 🌧️";
            case 66, 67 -> "Marznący deszcz 🌧️❄️";
            case 71, 73, 75 -> "Opady śniegu 🌨️";
            case 77 -> "Ziarna śniegu 🌨️";
            case 80, 81, 82 -> "Przelotny deszcz 🌦️";
            case 85, 86 -> "Przelotny śnieg 🌨️";
            case 95 -> "Burza ⚡";
            case 96, 99 -> "Burza z gradem ⚡🌨️";
            default -> "Nieznana pogoda 🤷";
        };
    }
}
