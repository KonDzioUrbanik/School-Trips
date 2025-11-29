package com.apiwosze.schooltrips.klasa;

import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping         //endpoint do pobrania wszystkich klas
    public List<KlasaModel> getAllKlasy(){
        return klasaService.getAllKlasy();
    }
    @PutMapping         //endpoint do aktualizacji profilu
    public KlasaModel updateProfil(@RequestBody KlasaDto klasaDto) {
        return klasaService.updateKlasy(klasaDto);
    }

    @PutMapping("/{id}")    //endpoint do aktualizacji nazwy klasy
    public KlasaModel updateNazwa(@PathVariable Long id, @RequestBody KlasaDto klasaDto) {
        return klasaService.updateNazwyKlasy(id, klasaDto);
    }

}
