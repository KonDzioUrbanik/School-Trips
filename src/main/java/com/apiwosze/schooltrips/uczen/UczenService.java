package com.apiwosze.schooltrips.uczen;

import org.springframework.stereotype.Service;

@Service
public class UczenService {
    private final UczenRepository uczenRepository;

    public UczenService(UczenRepository uczenRepository) {
        this.uczenRepository = uczenRepository;
    }
}
