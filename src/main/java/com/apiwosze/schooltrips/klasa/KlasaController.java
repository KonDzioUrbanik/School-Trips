package com.apiwosze.schooltrips.klasa;

import io.swagger.v3.oas.annotations.Operation;
import jdk.jfr.Description;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/klasy") // Główny endpoint odpowiada wszystkim poniżej
public class KlasaController {
    private final KlasaService klasaService;

    public KlasaController(KlasaService klasaService) {
        this.klasaService = klasaService;
    }


    @Operation(summary = "Dodawanie nowej klasy")
    @PostMapping        //endpoint do tworzenia klasy
    public KlasaModel createKlasa(@RequestBody KlasaDto klasaDto){ //Ta adnotacja mówi springowi że ma szukać w body/JSON a nie w adres URL
       return klasaService.createKlasa(klasaDto);
    }

    @Operation(summary = "Usuwanie klasy")
    @DeleteMapping      //endpoint do usuwania klasy
    public void deleteKlasa(@RequestBody KlasaDto klasaDto){
        klasaService.deleteKlasa(klasaDto);
    }

    @Operation(summary = "Pobieranie wszystkich klas")
    @GetMapping         //endpoint do pobrania wszystkich klas
    public List<KlasaModel> getAllKlasy(){
        return klasaService.getAllKlasy();
    }

    @Operation(summary = "Aktualizacja profilu klasy")
    @PutMapping         //endpoint do aktualizacji profilu
    public KlasaModel updateProfil(@RequestBody KlasaDto klasaDto) {
        return klasaService.updateProfilKlasy(klasaDto);
    }

    @Operation(summary = "Aktualizacja nazwy klasy")
    @PutMapping("/{id}")    //endpoint do aktualizacji nazwy klasy
    public KlasaModel updateNazwa(@PathVariable Long id, @RequestBody KlasaDto klasaDto) {
        return klasaService.updateNazwyKlasy(id, klasaDto);
    }

}
