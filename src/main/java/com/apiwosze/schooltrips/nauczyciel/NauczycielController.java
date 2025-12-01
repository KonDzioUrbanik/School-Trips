package com.apiwosze.schooltrips.nauczyciel;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nauczyciel")
public class NauczycielController {
    private final NauczycielService nauczycielService;

    public NauczycielController(NauczycielService nauczycielService) {
        this.nauczycielService = nauczycielService;
    }

    @GetMapping
    public List<NauczycielModel> getAllNauczyciele() {
        return nauczycielService.getAllNauczyciele();
    }

    @GetMapping("/{id}")
    public NauczycielModel getNauczycielById(@PathVariable Long id) {
        return nauczycielService.getNauczycielById(id);
    }

    @PostMapping
    public NauczycielModel createNauczyciel(@RequestBody NauczycielDto nauczycielDto) {
        return nauczycielService.createNauczyciel(nauczycielDto);
    }

    @DeleteMapping
    public void deleteNauczyciel(@RequestBody NauczycielDto nauczycielDto) {
        nauczycielService.deleteNauczyciel(nauczycielDto);
    }

    @PutMapping
    public NauczycielModel updatePrzedmiotAndTelefon(@RequestBody NauczycielDto nauczycielDto) {
        return nauczycielService.updatePrzedmiotAndTelefonNauczyciel(nauczycielDto);
    }

    @PutMapping("/{id}")
    public NauczycielModel updateImieAndNazwisko(@PathVariable Long id, @RequestBody NauczycielDto nauczycielDto) {
        return nauczycielService.updateImieAndNazwiskoNauczyciela(id, nauczycielDto);
    }
}