package com.apiwosze.schooltrips.wycieczka; // Definicja pakietu dla wycieczek

import com.apiwosze.schooltrips.common.BaseAuditEntity; // Import klasy bazowej audytu
import com.apiwosze.schooltrips.opiekun_wycieczki.OpiekunWycieczkiModel; // Import encji opiekunów wycieczki
import com.apiwosze.schooltrips.uczestnictwo.UczestnictwoModel; // Import encji uczestnictwa w wycieczce
import com.fasterxml.jackson.annotation.JsonIgnore; // Import adnotacji Jackson do ignorowania zapętlenia JSON-a
import jakarta.persistence.*; // Import adnotacji mapowania JPA
import lombok.Data; // Import Lombok @Data

import java.math.BigDecimal; // Import klasy do reprezentacji precyzyjnych kwot (koszt)
import java.time.LocalDate; // Import klasy daty Javy
import java.util.List; // Import listy Javy

@Data // Lombok automatycznie generuje gettery, settery, toString
@Entity // Oznacza klasę jako encję JPA powiązaną z tabelą bazy danych
@Table(name = "wycieczka") // Wskazanie na nazwę tabeli w bazie danych
public class WycieczkaModel extends BaseAuditEntity { // Dziedziczenie po klasie audytowej
    
    @Id // Klucz główny
    @GeneratedValue // Automatyczne generowanie wartości klucza głównego
    @Column(name = "id_wycieczki") // Mapowanie na kolumnę "id_wycieczki"
    private Long id; // Unikalny identyfikator wycieczki

    private String nazwa; // Nazwa wycieczki (np. "Wycieczka do Warszawy")
    private LocalDate data_rozpoczecia; // Data rozpoczęcia wycieczki
    private LocalDate data_zakonczenia; // Data zakończenia wycieczki
    private String miejsce_docelowe; // Miejsce docelowe wycieczki (np. "Warszawa")
    @Column(name = "koszt_na_osobe") // Mapowanie na kolumnę kosztów
    private BigDecimal koszt_na_osobe; // Koszt uczestnictwa dla jednej osoby

    @Column(columnDefinition = "TEXT")
    private String planWycieczki; // Harmonogram wycieczki wygenerowany przez AI

    @Enumerated(EnumType.STRING) // Mapowanie enuma statusu wycieczki jako tekst w bazie
    private Status status; // Aktualny status wycieczki (np. PLANOWANA, ZATWIERDZONA)

    @OneToMany(mappedBy = "wycieczka") // Relacja jeden-do-wielu: jedna wycieczka ma wielu zapisanych uczestników
    @JsonIgnore // Zabezpieczenie przed zapętleniem w JSON
    private List<UczestnictwoModel> uczestniczenie; // Lista rekordów uczestnictwa powiązanych z tą wycieczką

    @OneToMany(mappedBy = "wycieczkaOpiekun") // Relacja jeden-do-wielu: jedna wycieczka ma wielu przypisanych opiekunów
    @JsonIgnore // Zabezpieczenie przed zapętleniem w JSON
    private List<OpiekunWycieczkiModel> opiekunowie; // Lista opiekunów tej wycieczki
}