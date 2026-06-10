package com.apiwosze.schooltrips.uczestnictwo; // Definicja pakietu dla uczestnictwa w wycieczkach

import com.apiwosze.schooltrips.common.BaseAuditEntity; // Import klasy bazowej audytu
import com.apiwosze.schooltrips.uczen.UczenModel; // Import encji ucznia
import com.apiwosze.schooltrips.wycieczka.WycieczkaModel; // Import encji wycieczki
import com.apiwosze.schooltrips.zgoda_rodzica.ZgodaRodzicaModel; // Import encji zgody rodzica
import com.fasterxml.jackson.annotation.JsonIgnore; // Import adnotacji do ignorowania pola przy generowaniu JSON-a
import jakarta.persistence.*; // Import adnotacji mapowania JPA
import lombok.Data; // Import Lombok @Data

import java.time.LocalDate; // Import klasy daty Javy

@Data // Lombok automatycznie generuje gettery i settery
@Entity // Oznacza klasę jako encję JPA mapowaną na tabelę
@Table(name = "uczestnictwo") // Wskazanie nazwy tabeli w bazie danych
public class UczestnictwoModel extends BaseAuditEntity { // Dziedziczenie po klasie audytowej
    
    @Id // Klucz główny
    @GeneratedValue // Automatyczne generowanie wartości klucza głównego
    @Column(name = "id_uczestnictwa") // Mapowanie na kolumnę "id_uczestnictwa"
    private Long id; // Unikalny identyfikator uczestnictwa

    private LocalDate data_zapisania; // Data dokonania zapisu ucznia na wycieczkę
    private boolean czy_jedzie; // Flaga logiczna czy ostatecznie uczeń jedzie na wycieczkę
    private String uwagi; // Uwagi (np. alergie, specjalne wymagania żywieniowe)

    @ManyToOne // Relacja wiele-do-jednego: wielu uczniów uczestniczy w danej wycieczce
    @JoinColumn(name = "id_uczen") // Klucz obcy wskazujący na tabelę ucznia
    private UczenModel uczen; // Referencja do encji przypisanego ucznia

    @ManyToOne // Relacja wiele-do-jednego: w ramach jednej wycieczki jest wiele rekordów uczestnictwa
    @JoinColumn(name = "id_wycieczki") // Klucz obcy wskazujący na tabelę wycieczki
    private WycieczkaModel wycieczka; // Referencja do encji przypisanej wycieczki

    @OneToOne(mappedBy = "uczestnictwo") // Relacja jeden-do-jednego: jedno uczestnictwo ma dokładnie jedną powiązaną zgodę rodzica
    @JsonIgnore
    private ZgodaRodzicaModel zgoda_rodzica; // Referencja do encji zgody rodzica
}
