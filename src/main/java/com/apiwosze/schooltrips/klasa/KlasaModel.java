package com.apiwosze.schooltrips.klasa;

import com.apiwosze.schooltrips.uczen.UczenModel;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data //Taki skrót nie trzeba pisać getterów i setterów jest to po impl Lombok'a
@Entity//Powiedzenie springowi, że jest to Encja i ma jakby takie pola stworzyć
@Table(name = "klasa") // Jakby tego nie było to by nam stworzyło tabele o nazwie ModelKlasa
// ModelKlasa dlatego, aby było wszystko wiadome co gdzie jest MODEL, SERVICE, REPOSITORY
public class KlasaModel { //
    @Id
    @GeneratedValue
    private Long id_klasy; // Klucz podstawowy

    private String nazwa; //pole

    private String profil; //pole

    @OneToMany(mappedBy = "klasa")      //połączenie z tabelą uczeń relacją jeden do wielu (Jedna klasa do wielu uczniów)
    private List<UczenModel> uczniowie; //to jest takie jakby "Wyczytanie wszystkich WIELU uczniów" ..Many zazwyczaj jest List
}
