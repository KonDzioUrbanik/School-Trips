package com.apiwosze.schooltrips.zgoda_rodzica;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZgodaRodzicaRepository extends JpaRepository<ZgodaRodzicaModel, Long> {
}
