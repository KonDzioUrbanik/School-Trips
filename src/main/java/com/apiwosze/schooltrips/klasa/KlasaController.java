package com.apiwosze.schooltrips.klasa;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/klasy")
public class KlasaController {
    private final KlasaService klasaService;

    public KlasaController(KlasaService klasaService) {
        this.klasaService = klasaService;
    }

    @Operation(summary = "Dodawanie nowej klasy")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public KlasaModel createKlasa(@RequestBody @Valid KlasaDto klasaDto){
       return klasaService.createKlasa(klasaDto);
    }

    @Operation(summary = "Usuwanie klasy")
    @DeleteMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public void deleteKlasa(@RequestBody @Valid KlasaDto klasaDto){
        klasaService.deleteKlasa(klasaDto);
    }

    @Operation(summary = "Pobieranie wszystkich klas")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')")
    public List<KlasaModel> getAllKlasy(){
        return klasaService.getAllKlasy();
    }

    @Operation(summary = "Aktualizacja profilu klasy")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public KlasaModel updateProfil(@RequestBody @Valid KlasaDto klasaDto) {
        return klasaService.updateProfilKlasy(klasaDto);
    }

    @Operation(summary = "Aktualizacja nazwy klasy")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public KlasaModel updateNazwa(@PathVariable Long id, @RequestBody @Valid KlasaDto klasaDto) {
        return klasaService.updateNazwyKlasy(id, klasaDto);
    }
}
