package com.apiwosze.schooltrips.zgoda_rodzica;


import com.apiwosze.schooltrips.uczestnictwo.UczestnictwoModel;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "zgoda_rodzica")
public class ZgodaRodzicaModel {
    @Id
    @GeneratedValue
    @Column(name = "id_zgody")
    private Long id;
    private LocalDate data_podpisania;
    @Enumerated(EnumType.STRING)
    private Forma forma;
    private boolean czy_dostarczona;

    @OneToOne
    @JoinColumn(name = "id_uczestnictwa")
    private UczestnictwoModel uczestnictwo;
}
