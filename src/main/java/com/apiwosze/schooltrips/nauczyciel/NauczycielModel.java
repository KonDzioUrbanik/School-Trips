package com.apiwosze.schooltrips.nauczyciel;

import com.apiwosze.schooltrips.opiekun_wycieczki.OpiekunWycieczkiModel;
import com.fasterxml.jackson.annotation.JsonIgnore; // <--- Import
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
    @JsonIgnore // <--- DODAJ TO
    private List<OpiekunWycieczkiModel> opiekunowie;
}