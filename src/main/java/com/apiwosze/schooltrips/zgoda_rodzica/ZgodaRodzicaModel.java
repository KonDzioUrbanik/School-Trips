package com.apiwosze.schooltrips.zgoda_rodzica; // Definicja pakietu dla zgód rodziców

import com.apiwosze.schooltrips.common.BaseAuditEntity; // Import klasy bazowej audytu
import com.apiwosze.schooltrips.uczestnictwo.UczestnictwoModel; // Import encji uczestnictwa w wycieczce
import jakarta.persistence.*; // Import adnotacji mapowania JPA
import lombok.Data; // Import Lombok @Data

import java.time.LocalDate; // Import klasy daty Javy

@Data // Lombok automatycznie generuje gettery i settery
@Entity // Oznacza klasę jako encję JPA mapowaną na tabelę
@Table(name = "zgoda_rodzica") // Wskazanie na nazwę tabeli w bazie danych
public class ZgodaRodzicaModel extends BaseAuditEntity { // Dziedziczenie po klasie audytowej
    
    @Id // Klucz główny
    @GeneratedValue // Automatyczne generowanie wartości klucza głównego
    @Column(name = "id_zgody") // Mapowanie na kolumnę "id_zgody"
    private Long id; // Unikalny identyfikator zgody

    private LocalDate data_podpisania; // Data podpisania zgody przez rodzica

    @Enumerated(EnumType.STRING) // Mapowanie enuma formy dostarczenia jako tekst w bazie danych
    private Forma forma; // Forma dostarczenia zgody (np. PAPIEROWA, ELEKTRONICZNA)

    private boolean czy_dostarczona; // Flaga logiczna czy zgoda została faktycznie dostarczona do nauczyciela

    @OneToOne // Relacja jeden-do-jednego: zgoda rodzica jest przypisana do dokładnie jednego zapisu na wycieczkę
    @JoinColumn(name = "id_uczestnictwa") // Klucz obcy wskazujący na tabelę uczestnictwa
    private UczestnictwoModel uczestnictwo; // Referencja do encji uczestnictwa
}
