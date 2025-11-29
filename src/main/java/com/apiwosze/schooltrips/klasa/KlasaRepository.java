package com.apiwosze.schooltrips.klasa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KlasaRepository extends JpaRepository<KlasaModel, Long> {
    //Nasza metoda szukania po nazwie klasy (spring jest na tyle "mądry", że sam se ogarnie SQL)
    boolean existsByNazwa(String name);      //Nasza metoda sprawdzania po nazwie
    void deleteByNazwa(String nazwa);        //Nasza metoda usuwania po nazwie
}


//Ogólnie to używane jest JpaRepository dlatego że ma już troche gotowych metod których nie trzeba pisać
//Czas to pieniądz wiec mamy przy każdym interface repo gotowe metody m.in
//save() - zapisz / zaaktualizuj
//findAll() - pobierz wszystko
//deleteById() - usuń po id
//ułatwienia w życiu :D
