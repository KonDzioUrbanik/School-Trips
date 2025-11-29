package com.apiwosze.schooltrips.nauczyciel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NauczycielRepository extends JpaRepository<NauczycielModel, Long> {
}
