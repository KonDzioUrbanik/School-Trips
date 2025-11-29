package com.apiwosze.schooltrips.klasa;

import org.springframework.stereotype.Service;

@Service                                            //Tutaj mamy całą "Logikę biznesową" czyli tak na prawdę najwięcej kodu
public class KlasaService {                         //Będziemy opisywać tutaj to co ma robić dany moduł czyli to co nie daje nam JPA czyli potrzebne funkcje

    private final KlasaRepository klasaRepository;  //Tutaj masz wstrzykniętą baze danych dokładnie to tabele klase w tym przypadku,
                                                    // do wszystkich działań które musisz robić na bazie używasz klasaRepoitory(np. klasaRepository.findAll())


    public KlasaService(KlasaRepository klasaRepository) {
        this.klasaRepository = klasaRepository;
    }
}
