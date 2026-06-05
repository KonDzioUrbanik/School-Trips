package com.apiwosze.schooltrips.common; // Definicja pakietu dla klas wspólnych

import jakarta.persistence.Column; // Import adnotacji mapowania kolumn
import jakarta.persistence.EntityListeners; // Import adnotacji podpinającej słuchaczy zdarzeń encji
import jakarta.persistence.MappedSuperclass; // Import adnotacji oznaczającej klasę jako bazową dla innych encji (bez własnej tabeli)
import lombok.Data; // Import Lombok @Data dla getterów i setterów
import org.springframework.data.annotation.CreatedBy; // Import adnotacji oznaczającej pole twórcy rekordu
import org.springframework.data.annotation.CreatedDate; // Import adnotacji oznaczającej pole daty utworzenia rekordu
import org.springframework.data.annotation.LastModifiedBy; // Import adnotacji oznaczającej pole osoby edytującej rekord
import org.springframework.data.annotation.LastModifiedDate; // Import adnotacji oznaczającej pole daty ostatniej modyfikacji
import org.springframework.data.jpa.domain.support.AuditingEntityListener; // Import klasy nasłuchującej zdarzenia bazy danych w celu wstrzyknięcia audytu

import java.time.LocalDateTime; // Import klasy czasu lokalnego Javy

@Data // Lombok automatycznie wygeneruje gettery i settery dla pól audytowych
@MappedSuperclass // Klasa nie tworzy osobnej tabeli, lecz jej pola zostaną włączone do tabel klas dziedziczących
@EntityListeners(AuditingEntityListener.class) // Rejestracja słuchacza JPA Auditing, który uzupełnia pola przed zapisem do bazy
public abstract class BaseAuditEntity {

    @CreatedBy // Adnotacja Spring Data automatycznie wstawiająca zalogowanego użytkownika podczas operacji INSERT
    @Column(name = "created_by", updatable = false) // Kolumna w bazie, wyłączenie aktualizacji przy UPDATE (pole jest stałe)
    private String createdBy; // Nazwa użytkownika, który stworzył rekord

    @CreatedDate // Adnotacja Spring Data automatycznie wstawiająca aktualny czas serwera podczas operacji INSERT
    @Column(name = "created_at", updatable = false) // Kolumna w bazie, zablokowane modyfikacje przy edycji rekordu
    private LocalDateTime createdAt; // Dokładny moment utworzenia rekordu

    @LastModifiedBy // Adnotacja Spring Data automatycznie wstawiająca zalogowanego użytkownika podczas operacji UPDATE/INSERT
    @Column(name = "updated_by") // Kolumna w bazie danych na login edytującego
    private String updatedBy; // Nazwa użytkownika, który ostatnio zmodyfikował rekord

    @LastModifiedDate // Adnotacja Spring Data automatycznie wstawiająca aktualny czas serwera przy operacji UPDATE/INSERT
    @Column(name = "updated_at") // Kolumna w bazie danych na datę modyfikacji
    private LocalDateTime updatedAt; // Dokładny moment ostatniej edycji rekordu
}
