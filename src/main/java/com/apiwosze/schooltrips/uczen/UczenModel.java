package com.apiwosze.schooltrips.uczen;

import com.apiwosze.schooltrips.klasa.KlasaModel;
import com.apiwosze.schooltrips.uczestnictwo.UczestnictwoModel;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "uczen")
public class UczenModel {
    @Id
    @GeneratedValue
    @Column(name = "id_ucznia")
    private Long id; //Klucz podstawowy

    private String imie;    //pole

    private String nazwisko;    //pole

    private LocalDate data_urodzenia; //pole

    @ManyToOne
    @JoinColumn(name = "id_klasy") //Połączenie z tabelą klasa "Dołączenie", pozwolenie na połączenia i odwrotna relacja wiele uczniów do jednej klasy
    private KlasaModel klasa;

    @OneToMany(mappedBy = "uczen") //Połączenie z tabelą uczestnictwo relcja jeden do wielu (Jeden uczeń w wielu uczestnictwach w formularzu/wierszu
    private List<UczestnictwoModel> uczestniczenia;
}
