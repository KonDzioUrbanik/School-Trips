package com.apiwosze.schooltrips.security; // Definicja pakietu bezpieczeństwa

// Typ wyliczeniowy (enum) definiujący dostępne role użytkowników w systemie
public enum RolaUzytkownika {
    ADMIN,         // Rola administratora - pełny dostęp do wszystkich zasobów i modyfikacji klas/nauczycieli
    NAUCZYCIEL,    // Rola nauczyciela - możliwość przeglądania wycieczek oraz zarządzania ich opiekunami i uczestnikami
    UCZEN_RODZIC   // Rola ucznia lub rodzica - uprawnienia do odczytu danych oraz tworzenia uczestnictwa/zgód rodzicielskich
}