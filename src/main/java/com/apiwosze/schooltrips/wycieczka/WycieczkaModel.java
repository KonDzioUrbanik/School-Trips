package com.apiwosze.schooltrips.wycieczka;

import com.apiwosze.schooltrips.opiekun_wycieczki.OpiekunWycieczkiModel;
import com.apiwosze.schooltrips.uczestnictwo.UczestnictwoModel;
import com.fasterxml.jackson.annotation.JsonIgnore; // <--- WAŻNE: Dodaj ten import
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "wycieczka")
public class WycieczkaModel {
    @Id @GeneratedValue
    @Column(name = "id_wycieczki")
    private Long id;
    private String nazwa;
    private LocalDate data_rozpoczecia;
    private LocalDate data_zakonczenia;
    private String miejsce_docelowe;
    private BigDecimal koszt_na_osobe;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "wycieczka")
    @JsonIgnore // <--- DODAJ TO
    List<UczestnictwoModel> uczestniczenie;

    @OneToMany(mappedBy = "wycieczkaOpiekun")
    @JsonIgnore // <--- DODAJ TO
    private List<OpiekunWycieczkiModel> opiekunowie;
}