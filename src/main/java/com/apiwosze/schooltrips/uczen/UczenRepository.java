package com.apiwosze.schooltrips.uczen;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UczenRepository extends JpaRepository<UczenModel, Long> {
}
