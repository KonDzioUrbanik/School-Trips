package com.apiwosze.schooltrips.klasa;

import com.apiwosze.schooltrips.uczen.UczenModel;
import com.fasterxml.jackson.annotation.JsonIgnore; // <--- Import
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "klasa")
public class KlasaModel {
    @Id
    @GeneratedValue
    @Column(name = "id_klasy")
    private Long id;

    private String nazwa;
    private String profil;

    @OneToMany(mappedBy = "klasa")
    @JsonIgnore // <--- DODAJ TO
    private List<UczenModel> uczniowie;
}