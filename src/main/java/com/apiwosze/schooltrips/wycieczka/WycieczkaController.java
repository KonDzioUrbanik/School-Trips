package com.apiwosze.schooltrips.wycieczka;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wycieczka")
public class WycieczkaController {
    private final WycieczkaService wycieczkaService;
    private final AiService aiService;

    public WycieczkaController(WycieczkaService wycieczkaService, AiService aiService) {
        this.wycieczkaService = wycieczkaService;
        this.aiService = aiService;
    }

    @PostMapping("/{id}/generuj-plan")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public WycieczkaModel generujPlan(@PathVariable Long id) {
        WycieczkaModel wycieczka = wycieczkaService.getWycieczkaById(id);
        String plan = aiService.generateTripPlan(
                wycieczka.getNazwa(),
                wycieczka.getMiejsce_docelowe(),
                wycieczka.getData_rozpoczecia(),
                wycieczka.getData_zakonczenia()
        );
        WycieczkaDto dto = new WycieczkaDto(
                wycieczka.getNazwa(),
                wycieczka.getData_rozpoczecia(),
                wycieczka.getData_zakonczenia(),
                wycieczka.getMiejsce_docelowe(),
                wycieczka.getKoszt_na_osobe(),
                wycieczka.getStatus(),
                plan
        );
        return wycieczkaService.updateWycieczka(id, dto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')")
    public List<WycieczkaModel> getAllWycieczki(){
        return wycieczkaService.getAllWycieczki();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')")
    public WycieczkaModel getWycieczkaById(@PathVariable Long id){
        return wycieczkaService.getWycieczkaById(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public void deleteWycieczka(@PathVariable Long id){
        wycieczkaService.deleteWycieczka(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public WycieczkaModel updateWycieczka(@PathVariable Long id, @RequestBody @Valid WycieczkaDto wycieczkaDto){
        return wycieczkaService.updateWycieczka(id, wycieczkaDto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public WycieczkaModel createWycieczka(@RequestBody @Valid WycieczkaDto wycieczkaDto){
        return wycieczkaService.createWycieczka(wycieczkaDto);
    }
}
