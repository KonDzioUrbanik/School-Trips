package com.apiwosze.schooltrips.wycieczka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class AiService {

    @Value("${schooltrips.gemini.api-key:}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String generateTripPlan(String nazwa, String miejsceDocelowe, LocalDate dataRozpoczecia, LocalDate dataZakonczenia) {
        long dni = ChronoUnit.DAYS.between(dataRozpoczecia, dataZakonczenia) + 1;
        if (dni <= 0) {
            dni = 1;
        }

        // Przygotowanie promptu
        String prompt = String.format(
                "Jesteś doświadczonym przewodnikiem turystycznym i organizatorem wycieczek szkolnych. " +
                "Stwórz ciekawy, bezpieczny i edukacyjny plan wycieczki szkolnej o nazwie '%s' do miejsca '%s' " +
                "w dniach od %s do %s (łącznie dni: %d). " +
                "Plan powinien być ustrukturyzowany dzień po dniu, zawierający orientacyjne godziny (np. 8:00 - Śniadanie, 9:30 - Wyjście itp.), " +
                "dostosowany do uczniów w wieku szkolnym. " +
                "Zwróć wynik w czytelnym formacie Markdown (używaj nagłówków, pogrubień, list punktowanych). Nie dodawaj wstępów ani komentarzy pobocznych, tylko czysty plan.",
                nazwa, miejsceDocelowe, dataRozpoczecia, dataZakonczenia, dni
        );

        if (apiKey == null || apiKey.trim().isEmpty() || 
            apiKey.contains("${") || 
            apiKey.equalsIgnoreCase("undefined") || 
            apiKey.equalsIgnoreCase("null") || 
            apiKey.equalsIgnoreCase("placeholder") ||
            apiKey.trim().length() < 10) {
            System.out.println("GEMINI_API_KEY nie jest skonfigurowany lub jest nieprawidłowy. Generowanie planu próbnego (Mock Fallback).");
            return generateMockPlan(nazwa, miejsceDocelowe, dataRozpoczecia, dataZakonczenia, dni);
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

            // Tworzenie JSON za pomocą klas pomocniczych
            GeminiRequest requestPayload = new GeminiRequest(prompt);
            String requestBody = objectMapper.writeValueAsString(requestPayload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(java.time.Duration.ofSeconds(35)) // Limit czasu żądania 35s
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.body());
                JsonNode textNode = rootNode.path("candidates")
                        .path(0)
                        .path("content")
                        .path("parts")
                        .path(0)
                        .path("text");
                
                if (!textNode.isMissingNode()) {
                    return textNode.asText();
                }
            }

            System.err.println("Gemini API error. Status: " + response.statusCode() + ", Body: " + response.body());
            return "### ⚠️ Plan wycieczki (Mock Fallback - błąd zewnętrznego API)\n\n" +
                   "*Nie udało się połączyć z API Gemini (Status " + response.statusCode() + "). Poniżej znajduje się automatycznie wygenerowany plan zastępczy.*\n\n" +
                   generateMockPlan(nazwa, miejsceDocelowe, dataRozpoczecia, dataZakonczenia, dni);

        } catch (Exception e) {
            System.err.println("Wystąpił błąd podczas żądania do Gemini API: " + e.getMessage());
            return "### ⚠️ Plan wycieczki (Mock Fallback - wyjątek połączenia)\n\n" +
                   "*Wystąpił problem techniczny podczas generowania planu AI: " + e.getMessage() + ". Poniżej znajduje się automatycznie wygenerowany plan zastępczy.*\n\n" +
                   generateMockPlan(nazwa, miejsceDocelowe, dataRozpoczecia, dataZakonczenia, dni);
        }
    }

    private String fetchAvailableModels() {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.body());
                JsonNode modelsNode = rootNode.path("models");
                if (modelsNode.isArray()) {
                    java.util.List<String> modelNames = new java.util.ArrayList<>();
                    for (JsonNode model : modelsNode) {
                        modelNames.add(model.path("name").asText().replace("models/", ""));
                    }
                    return modelNames.toString();
                }
                return "Brak listy modeli w odpowiedzi.";
            } else {
                return "Błąd " + response.statusCode() + ": " + response.body();
            }
        } catch (Exception e) {
            return "Wyjątek: " + e.getMessage();
        }
    }

    private String generateMockPlan(String nazwa, String miejsceDocelowe, LocalDate start, LocalDate end, long dni) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("# 🎒 Plan Wycieczki: %s (%s)\n\n", nazwa, miejsceDocelowe));
        sb.append(String.format("**Czas trwania:** %s do %s (%d dni)\n\n", start, end, dni));
        sb.append("> ⚠️ *Uwaga: To jest automatyczny plan demonstracyjny (brak skonfigurowanego klucza GEMINI_API_KEY lub błąd połączenia).* \n\n");

        for (int i = 1; i <= dni; i++) {
            sb.append(String.format("### 📅 Dzień %d\n", i));
            if (i == 1) {
                sb.append("- **08:00** – Zbiórka uczestników przed budynkiem szkoły i załadunek bagaży.\n");
                sb.append("- **08:30** – Odjazd autokaru w kierunku celu podróży.\n");
                sb.append(String.format("- **12:30** – Przyjazd do **%s**, zakwaterowanie w ośrodku wypoczynkowym.\n", miejsceDocelowe));
                sb.append("- **13:30** – Wspólny obiad.\n");
                sb.append("- **15:00** – Spacer zapoznawczy po okolicy z przewodnikiem i omówienie zasad bezpieczeństwa.\n");
                sb.append("- **18:30** – Kolacja.\n");
                sb.append("- **20:00** – Wieczór integracyjny (podział na grupy, gry i konkursy).\n");
            } else if (i == dni) {
                sb.append("- **08:30** – Śniadanie.\n");
                sb.append("- **10:00** – Wykwaterowanie z pokoi i pakowanie bagaży.\n");
                sb.append("- **11:00** – Zakup pamiątek i ostatni spacer rekreacyjny po okolicy.\n");
                sb.append("- **13:00** – Obiad pożegnalny.\n");
                sb.append("- **14:00** – Wyjazd w drogę powrotną.\n");
                sb.append("- **18:00** – Planowany powrót pod szkołę i odbiór uczniów przez rodziców.\n");
            } else {
                sb.append("- **08:30** – Śniadanie.\n");
                sb.append("- **09:30** – Wyjście na całodniowe zwiedzanie najciekawszych atrakcji turystycznych i zabytków.\n");
                sb.append("- **13:30** – Lunch w formie suchego prowiantu w plenerze.\n");
                sb.append("- **14:30** – Warsztaty edukacyjne / zajęcia tematyczne na miejscu.\n");
                sb.append("- **16:30** – Powrót do ośrodka, czas wolny na odpoczynek i gry sportowe.\n");
                sb.append("- **18:30** – Kolacja.\n");
                sb.append("- **19:30** – Wspólne ognisko z pieczeniem kiełbasek i śpiewaniem piosenek.\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // Klasy pomocnicze do poprawnej struktury JSON dla Gemini API
    public static class GeminiRequest {
        public Content[] contents;

        public GeminiRequest(String promptText) {
            this.contents = new Content[]{ new Content(promptText) };
        }
    }

    public static class Content {
        public Part[] parts;

        public Content(String promptText) {
            this.parts = new Part[]{ new Part(promptText) };
        }
    }

    public static class Part {
        public String text;

        public Part(String promptText) {
            this.text = promptText;
        }
    }
}
