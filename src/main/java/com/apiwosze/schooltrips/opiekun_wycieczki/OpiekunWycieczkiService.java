package com.apiwosze.schooltrips.opiekun_wycieczki;

import org.springframework.stereotype.Service;

@Service
public class OpiekunWycieczkiService {
    private final OpiekunWycieczkiRepository opiekunWycieczkiRepository;

    public OpiekunWycieczkiService(OpiekunWycieczkiRepository opiekunWycieczkiRepository) {
        this.opiekunWycieczkiRepository = opiekunWycieczkiRepository;

    }
}
