package com.apiwosze.schooltrips.klasa; // Definicja pakietu dla modułu Klasy

import com.apiwosze.schooltrips.common.BaseAuditEntity; // Import klasy bazowej audytu
import com.apiwosze.schooltrips.uczen.UczenModel; // Import encji ucznia powiązanej relacją
import com.fasterxml.jackson.annotation.JsonIgnore; // Import adnotacji zapobiegającej nieskończonej pętli przy serializacji JSON-a
import jakarta.persistence.*; // Import adnotacji mapowania JPA
import lombok.Data; // Import Lombok @Data dla getterów/setterów
import java.util.List; // Import listy Javy

@Data // Wygenerowanie metod dostępowych getter/setter/toString przez Lombok
@Entity // Oznacza klasę jako encję reprezentującą tabelę w bazie danych
@Table(name = "klasa") // Wskazanie na nazwę tabeli w bazie danych jako "klasa"
public class KlasaModel extends BaseAuditEntity {
    
    @Id // Oznaczenie pola jako klucza głównego
    @GeneratedValue // Automatyczne generowanie wartości klucza głównego
    @Column(name = "id_klasy") // Zmapowanie pola na kolumnę "id_klasy" w bazie
    private Long id; // Unikalny identyfikator klasy

    private String nazwa; // Pole przechowujące nazwę klasy (np. "1A")
    private String profil; // Pole przechowujące profil klasy (np. "humanistyczny")

    // Relacja jeden-do-wielu: jedna klasa może mieć wielu uczniów. MappedBy wskazuje pole w klasie UczenModel zarządzające relacją
    @OneToMany(mappedBy = "klasa") 
    @JsonIgnore // Ignorowanie listy uczniów przy generowaniu JSON-a dla klasy, zapobiega to zapętleniu (Klasa -> Uczniowie -> Klasa...)
    private List<UczenModel> uczniowie; // Lista uczniów należących do tej klasy
}