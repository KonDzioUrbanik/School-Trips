package com.apiwosze.schooltrips.zgoda_rodzica;

import org.springframework.stereotype.Service;

@Service
public class ZgodaRodzicaService {
    private final ZgodaRodzicaRepository zgodaRodzicaRepository;

    public ZgodaRodzicaService(ZgodaRodzicaRepository zgodaRodzicaRepository) {
        this.zgodaRodzicaRepository = zgodaRodzicaRepository;
    }
}
