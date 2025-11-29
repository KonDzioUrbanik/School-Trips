package com.apiwosze.schooltrips.klasa;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/klasy") // Główny endpoint odpowiada wszystkim poniżej
public class KlasaController {
    private final KlasaService klasaService;

    public KlasaController(KlasaService klasaService) {
        this.klasaService = klasaService;
    }

    @PostMapping        //endpoint do tworzenia klasy
    public KlasaModel createKlasa(@RequestBody KlasaDto klasaDto){ //Ta adnotacja mówi springowi że ma szukać w body/JSON a nie w adres URL
       return klasaService.addKlasa(klasaDto);
    }

    @DeleteMapping      //endpoint do usuwania klasy
    public void deleteKlasa(@RequestBody KlasaDto klasaDto){
        klasaService.deleteKlasa(klasaDto);
    }

}
