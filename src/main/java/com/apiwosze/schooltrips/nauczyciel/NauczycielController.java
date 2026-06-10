package com.apiwosze.schooltrips.nauczyciel;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')")
    public List<NauczycielModel> getAllNauczyciele() {
        return nauczycielService.getAllNauczyciele();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')")
    public NauczycielModel getNauczycielById(@PathVariable Long id) {
        return nauczycielService.getNauczycielById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public NauczycielModel createNauczyciel(@RequestBody @Valid NauczycielDto nauczycielDto) {
        return nauczycielService.createNauczyciel(nauczycielDto);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteNauczyciel(@RequestBody @Valid NauczycielDto nauczycielDto) {
        nauczycielService.deleteNauczyciel(nauczycielDto);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public NauczycielModel updatePrzedmiotAndTelefon(@RequestBody @Valid NauczycielDto nauczycielDto) {
        return nauczycielService.updatePrzedmiotAndTelefonNauczyciel(nauczycielDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public NauczycielModel updateImieAndNazwisko(@PathVariable Long id, @RequestBody @Valid NauczycielDto nauczycielDto) {
        return nauczycielService.updateImieAndNazwiskoNauczyciela(id, nauczycielDto);
    }
}