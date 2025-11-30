package com.apiwosze.schooltrips.opiekun_wycieczki;

import com.apiwosze.schooltrips.nauczyciel.NauczycielModel;
import com.apiwosze.schooltrips.wycieczka.WycieczkaModel;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "opiekun_wycieczki")
public class OpiekunWycieczkiModel {
    @Id
    @GeneratedValue
    @Column(name = "id_opiekun_wycieczki")
    private Long id;

    @Enumerated(EnumType.STRING)
    private Rola rola;

    @ManyToOne
    @JoinColumn(name = "id_wycieczki")
    private WycieczkaModel wycieczkaOpiekun;

    @ManyToOne
    @JoinColumn(name = "id_nauczyciela")
    private NauczycielModel nauczyciel;
}
