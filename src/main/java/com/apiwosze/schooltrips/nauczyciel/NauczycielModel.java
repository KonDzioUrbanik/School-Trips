package com.apiwosze.schooltrips.nauczyciel; // Definicja pakietu dla modułu Nauczycieli

import com.apiwosze.schooltrips.common.BaseAuditEntity; // Import klasy bazowej audytu
import com.apiwosze.schooltrips.opiekun_wycieczki.OpiekunWycieczkiModel; // Import encji łączącej nauczyciela z rolą opiekuna wycieczki
import com.fasterxml.jackson.annotation.JsonIgnore; // Import adnotacji zapobiegającej pętli serializacji JSON
import jakarta.persistence.*; // Import adnotacji mapowania JPA
import lombok.Data; // Import Lombok @Data
import java.util.List; // Import listy Javy

@Data // Lombok automatycznie generuje gettery, settery, toString, equals i hashCode
@Entity // Oznacza klasę jako encję powiązaną z tabelą bazy danych
@Table(name = "nauczyciel") // Wskazanie nazwy tabeli w bazie danych
public class NauczycielModel extends BaseAuditEntity {
    
    @Id // Oznaczenie klucza głównego
    @GeneratedValue // Automatyczne generowanie wartości klucza głównego
    @Column(name = "id_nauczyciela") // Mapowanie pola id na kolumnę "id_nauczyciela"
    private Long id; // Identyfikator nauczyciela

    private String imie; // Pole imię nauczyciela
    private String nazwisko; // Pole nazwisko nauczyciela
    private String przedmiot; // Pole nauczany przedmiot
    private String telefon_kontaktowy; // Pole numer telefonu nauczyciela

    // Relacja jeden-do-wielu: jeden nauczyciel może pełnić rolę opiekuna na wielu wycieczkach
    @OneToMany(mappedBy = "nauczyciel") // Pole "nauczyciel" w klasie OpiekunWycieczkiModel jest właścicielem tej relacji
    @JsonIgnore // Zabezpieczenie przed nieskończonym zagnieżdżeniem JSON-a
    private List<OpiekunWycieczkiModel> opiekunowie; // Lista wycieczek, na których ten nauczyciel jest opiekunem
}