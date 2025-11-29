package com.apiwosze.schooltrips.wycieczka;

import org.springframework.stereotype.Service;

@Service
public class WycieczkaService {
    private final WycieczkaRepository wycieczkaRepository;

    public WycieczkaService(WycieczkaRepository wycieczkaRepository) {
        this.wycieczkaRepository = wycieczkaRepository;
    }
}
