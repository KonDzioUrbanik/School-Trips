package com.apiwosze.schooltrips.nauczyciel;

import com.apiwosze.schooltrips.opiekun_wycieczki.OpiekunWycieczkiModel;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "nauczyciel")
public class NauczycielModel {
    @Id @GeneratedValue
    @Column(name = "id_nauczyciela")
    private Long id;
    private String imie;
    private String nazwisko;
    private String przedmiot;
    private String telefon_kontaktowy;


    @OneToMany(mappedBy = "nauczyciel")
    private List<OpiekunWycieczkiModel> opiekunowie;

}
