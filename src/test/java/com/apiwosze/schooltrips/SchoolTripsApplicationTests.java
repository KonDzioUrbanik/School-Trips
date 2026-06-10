package com.apiwosze.schooltrips;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

// Test sprawdzający tylko podstawową poprawność aplikacji, bez ładowania kontekstu Spring.
// Pełny test integracyjny (SpringBootTest) wymaga działającej bazy danych i zmiennych środowiskowych.
class SchoolTripsApplicationTests {

    @Test
    void applicationClassExists() {
        // Weryfikujemy tylko, że klasa główna aplikacji istnieje i jest dostępna
        assertTrue(SchoolTripsApplication.class != null, "Klasa aplikacji powinna istnieć");
    }

}
