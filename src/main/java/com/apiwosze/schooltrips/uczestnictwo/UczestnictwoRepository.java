package com.apiwosze.schooltrips.uczestnictwo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UczestnictwoRepository extends JpaRepository<UczestnictwoModel, Long> {
}
