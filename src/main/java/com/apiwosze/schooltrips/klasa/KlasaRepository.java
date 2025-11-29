package com.apiwosze.schooltrips.klasa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KlasaRepository extends JpaRepository<KlasaModel, Long> {
}


//Ogólnie to używane jest JpaRepository dlatego że ma już troche gotowych metod których nie trzeba pisać
//Czas to pieniądz wiec mamy przy każdym interface repo gotowe metody m.in
//save() - zapisz / zaaktualizuj
//findAll() - pobierz wszystko
//deleteById() - usuń po id
//ułatwienia w życiu :D
