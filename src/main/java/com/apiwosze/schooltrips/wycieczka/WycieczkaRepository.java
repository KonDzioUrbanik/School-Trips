package com.apiwosze.schooltrips.wycieczka;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WycieczkaRepository extends JpaRepository<WycieczkaModel, Long> {
}
