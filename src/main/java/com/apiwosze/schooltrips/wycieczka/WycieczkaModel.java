package com.apiwosze.schooltrips.wycieczka;

import com.apiwosze.schooltrips.opiekun_wycieczki.OpiekunWycieczkiModel;
import com.apiwosze.schooltrips.uczestnictwo.UczestnictwoModel;
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
    private Long id_wycieczki;
    private String nazwa;
    private LocalDate data_rozpoczecia;
    private LocalDate data_zakonczenia;
    private String miejsce_docelowe;
    private BigDecimal koszt_na_osobe;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "wycieczka")
    List<UczestnictwoModel> uczestniczenie;

    @OneToMany(mappedBy = "wycieczkaOpiekun")
    private List<OpiekunWycieczkiModel> opiekunowie;


}
