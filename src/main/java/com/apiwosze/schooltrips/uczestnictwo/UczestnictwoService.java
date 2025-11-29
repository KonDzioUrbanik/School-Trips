package com.apiwosze.schooltrips.uczestnictwo;

import org.springframework.stereotype.Service;

@Service
public class UczestnictwoService {
    private final UczestnictwoRepository uczestnictwoRepository;

    public UczestnictwoService(UczestnictwoRepository uczestnictwoRepository) {
        this.uczestnictwoRepository = uczestnictwoRepository;
    }
}
