package com.apiwosze.schooltrips.opiekun_wycieczki; // Definicja pakietu opiekunów wycieczki

import com.apiwosze.schooltrips.nauczyciel.NauczycielModel; // Import encji nauczyciela
import com.apiwosze.schooltrips.wycieczka.WycieczkaModel; // Import encji wycieczki
import jakarta.persistence.*; // Import adnotacji mapowania JPA
import lombok.Data; // Import Lombok @Data

@Data // Adnotacja Lombok generująca gettery, settery, toString, equals i hashCode
@Entity // Oznacza klasę jako encję reprezentującą tabelę w bazie danych
@Table(name = "opiekun_wycieczki") // Wskazuje nazwę tabeli w bazie danych
public class OpiekunWycieczkiModel {
    
    @Id // Klucz główny encji
    @GeneratedValue // Automatyczne generowanie wartości klucza głównego
    @Column(name = "id_opiekun_wycieczki") // Mapowanie na kolumnę "id_opiekun_wycieczki"
    private Long id; // Unikalny identyfikator opiekuna na wycieczce

    @Enumerated(EnumType.STRING) // Mapowanie enuma roli jako tekst w bazie danych
    private Rola rola; // Rola opiekuna (KIEROWNIK lub OPIEKUN)

    @ManyToOne // Relacja wiele-do-jednego: wielu opiekunów może być przypisanych do jednej wycieczki
    @JoinColumn(name = "id_wycieczki") // Nazwa kolumny klucza obcego łączącej z tabelą wycieczka
    private WycieczkaModel wycieczkaOpiekun; // Referencja do encji wycieczki

    @ManyToOne // Relacja wiele-do-jednego: ten sam nauczyciel może być opiekunem na wielu wycieczkach
    @JoinColumn(name = "id_nauczyciela") // Nazwa kolumny klucza obcego łączącej z tabelą nauczyciel
    private NauczycielModel nauczyciel; // Referencja do encji nauczyciela
}
