package com.apiwosze.schooltrips.uczen;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/uczen")
public class UczenController {
    private final UczenService uczenService;

    public UczenController(UczenService uczenService) {
        this.uczenService = uczenService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')")
    public UczenModel getUczenById(@PathVariable Long id){
        return uczenService.getUczenById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')")
    public List<UczenModel> getAllUczen(){
        return uczenService.getAllUczen();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public UczenModel createUczen(@RequestBody @Valid UczenDto uczenDto){
        return uczenService.createUczen(uczenDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public void deleteUczen(@PathVariable Long id){
        uczenService.deleteUczen(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public UczenModel updateUczen(@PathVariable Long id, @RequestBody @Valid UczenDto uczenDto){
        return uczenService.updateUczen(id, uczenDto);
    }
}
