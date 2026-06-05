package com.apiwosze.schooltrips.opiekun_wycieczki;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/opiekun_wycieczki")
public class OpiekunWycieczkiController {
    final private OpiekunWycieczkiService opiekunWycieczkiService;

    public OpiekunWycieczkiController(OpiekunWycieczkiService opiekunWycieczkiService) {
        this.opiekunWycieczkiService = opiekunWycieczkiService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')")
    public OpiekunWycieczkiModel getOpiekunById(@PathVariable Long id) {
        return opiekunWycieczkiService.getOpiekunWycieczkiById(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public void deleteOpiekun(@PathVariable Long id){
        opiekunWycieczkiService.deleteOpiekunWycieczki(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public OpiekunWycieczkiModel updateOpiekun(@PathVariable Long id, @RequestBody @Valid OpiekunWycieczkiDto opiekunWycieczkiDto){
        return opiekunWycieczkiService.updateOpiekunWycieczki(id, opiekunWycieczkiDto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')")
    public List<OpiekunWycieczkiModel> getAllOpiekun(){
        return opiekunWycieczkiService.getAllOpiekunowie();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public OpiekunWycieczkiModel createOpiekun(@RequestBody @Valid OpiekunWycieczkiDto opiekunWycieczkiDto){
        return opiekunWycieczkiService.createOpiekunWycieczki(opiekunWycieczkiDto);
    }
}
