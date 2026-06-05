package com.apiwosze.schooltrips.wycieczka;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wycieczka")
public class WycieczkaController {
    private final WycieczkaService wycieczkaService;

    public WycieczkaController(WycieczkaService wycieczkaService) {
        this.wycieczkaService = wycieczkaService;
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
