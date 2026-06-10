package com.apiwosze.schooltrips.uczen; // Definicja pakietu dla uczniów

import com.apiwosze.schooltrips.common.BaseAuditEntity; // Import klasy bazowej audytu
import com.apiwosze.schooltrips.klasa.KlasaModel; // Import encji klasy
import com.apiwosze.schooltrips.uczestnictwo.UczestnictwoModel; // Import encji uczestnictwa w wycieczkach
import com.fasterxml.jackson.annotation.JsonIgnore; // Import adnotacji do ignorowania pola przy generowaniu JSON-a
import jakarta.persistence.*; // Import adnotacji mapowania JPA
import lombok.Data; // Import Lombok @Data
import java.time.LocalDate; // Import klasy daty Javy
import java.util.List; // Import listy Javy

@Data // Generowanie getterów, setterów, toString przez Lombok
@Entity // Oznacza klasę jako encję reprezentującą tabelę w bazie danych
@Table(name = "uczen") // Wskazanie na nazwę tabeli w bazie danych
public class UczenModel extends BaseAuditEntity { // Dziedziczenie po klasie audytowej
    
    @Id // Oznaczenie klucza głównego
    @GeneratedValue // Automatyczne generowanie wartości klucza głównego
    @Column(name = "id_ucznia") // Mapowanie na kolumnę "id_ucznia"
    private Long id; // Unikalny identyfikator ucznia

    private String imie; // Pole przechowujące imię ucznia
    private String nazwisko; // Pole przechowujące nazwisko ucznia
    private LocalDate data_urodzenia; // Pole przechowujące datę urodzenia ucznia

    @ManyToOne // Relacja wiele-do-jednego: wielu uczniów należy do jednej klasy
    @JoinColumn(name = "id_klasy") // Klucz obcy wskazujący na tabelę klasa
    private KlasaModel klasa; // Referencja do encji klasy ucznia

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private com.apiwosze.schooltrips.security.Uzytkownik user; // Powiązane konto użytkownika

    @OneToMany(mappedBy = "uczen") // Relacja jeden-do-wielu: jeden uczeń może uczestniczyć w wielu wycieczkach
    @JsonIgnore // Zabezpieczenie przed zapętleniem przy tworzeniu JSON-a
    private List<UczestnictwoModel> uczestniczenia; // Lista wycieczek ucznia
}