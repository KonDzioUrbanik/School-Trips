package com.apiwosze.schooltrips.opiekun_wycieczki;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpiekunWycieczkiRepository extends JpaRepository<OpiekunWycieczkiModel, Long> {
}
