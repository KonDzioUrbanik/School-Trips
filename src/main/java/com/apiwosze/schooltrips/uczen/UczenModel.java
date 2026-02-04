package com.apiwosze.schooltrips.uczen;

import com.apiwosze.schooltrips.klasa.KlasaModel;
import com.apiwosze.schooltrips.uczestnictwo.UczestnictwoModel;
import com.fasterxml.jackson.annotation.JsonIgnore; // <--- Import
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
    private Long id;

    private String imie;
    private String nazwisko;
    private LocalDate data_urodzenia;

    @ManyToOne
    @JoinColumn(name = "id_klasy")
    private KlasaModel klasa;

    @OneToMany(mappedBy = "uczen")
    @JsonIgnore // <--- DODAJ TO
    private List<UczestnictwoModel> uczestniczenia;
}