package com.apiwosze.schooltrips.zgoda_rodzica;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zgoda_rodzica")
public class ZgodaRodzicaController {
    private final ZgodaRodzicaService zgodaRodzicaService;

    public ZgodaRodzicaController(ZgodaRodzicaService zgodaRodzicaService) {
        this.zgodaRodzicaService = zgodaRodzicaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')")
    public List<ZgodaRodzicaModel> getAllZgodaRodzica(){
        return zgodaRodzicaService.getAllZgodaRodzica();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')")
    public ZgodaRodzicaModel getZgodaRodzicaById(@PathVariable Long id){
        return zgodaRodzicaService.getZgodaRodzicaById(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public void deleteZgodaRodzica(@PathVariable Long id){
        zgodaRodzicaService.deleteZgodaRodzica(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public ZgodaRodzicaModel updateZgodaRodzica(@PathVariable Long id, @RequestBody @Valid ZgodaRodzicaDto zgodaRodzicaDto){
        return zgodaRodzicaService.updateZgodaRodzica(id, zgodaRodzicaDto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')")
    public ZgodaRodzicaModel createZgodaRodzica(@RequestBody @Valid ZgodaRodzicaDto zgodaRodzicaDto){
        return zgodaRodzicaService.createZgodaRodzica(zgodaRodzicaDto);
    }
}
