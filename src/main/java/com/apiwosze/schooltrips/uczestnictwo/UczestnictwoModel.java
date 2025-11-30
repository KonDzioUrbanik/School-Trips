package com.apiwosze.schooltrips.uczestnictwo;

import com.apiwosze.schooltrips.uczen.UczenModel;
import com.apiwosze.schooltrips.wycieczka.WycieczkaModel;
import com.apiwosze.schooltrips.zgoda_rodzica.ZgodaRodzicaModel;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "uczestnictwo")
public class UczestnictwoModel {
    @Id @GeneratedValue
    @Column(name = "id_uczestnictwa")
    private Long id;
    private LocalDate data_zapisania;
    private boolean czy_jedzie;
    private String uwagi;

    @ManyToOne
    @JoinColumn(name = "id_uczen")
    private UczenModel uczen;

    @ManyToOne
    @JoinColumn(name = "id_wycieczki")
    private WycieczkaModel wycieczka;

    @OneToOne(mappedBy = "uczestnictwo")
    private ZgodaRodzicaModel zgoda_rodzica;
}
