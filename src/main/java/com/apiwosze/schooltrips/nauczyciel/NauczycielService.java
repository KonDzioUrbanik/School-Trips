package com.apiwosze.schooltrips.nauczyciel;

import org.springframework.stereotype.Service;

@Service
public class NauczycielService {
    private final NauczycielRepository nauczycielRepository;

    public NauczycielService(NauczycielRepository nauczycielRepository) {
        this.nauczycielRepository = nauczycielRepository;
    }
}
