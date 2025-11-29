package com.apiwosze.schooltrips.klasa;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service                                            //Tutaj mamy całą "Logikę biznesową" czyli tak na prawdę najwięcej kodu
public class KlasaService {                         //Będziemy opisywać tutaj to co ma robić dany moduł czyli to co nie daje nam JPA czyli potrzebne funkcje

    private final KlasaRepository klasaRepository;  //Tutaj masz wstrzykniętą baze danych dokładnie to tabele klase w tym przypadku,
                                                    // do wszystkich działań które musisz robić na bazie używasz klasaRepoitory(np. klasaRepository.findAll())


    public KlasaService(KlasaRepository klasaRepository) {
        this.klasaRepository = klasaRepository;
    }

    public KlasaModel addKlasa(KlasaDto klasaDto) {
        if (klasaRepository.existsByNazwa(klasaDto.nazwa())){
            throw new IllegalArgumentException("Klasa o takiej nazwie już istnieje!");
        }
        else {
            KlasaModel klasaModel = new KlasaModel();
            klasaModel.setNazwa(klasaDto.nazwa());
            klasaModel.setProfil(klasaDto.profil());
            return klasaRepository.save(klasaModel);
        }
    }

    @Transactional
    public void deleteKlasa(KlasaDto klasaDto){
        if(!klasaRepository.existsByNazwa(klasaDto.nazwa())){
            throw new IllegalArgumentException("Nie ma takiej klasy!");
        }
        else {
            klasaRepository.deleteByNazwa(klasaDto.nazwa());
        }
    }


}
