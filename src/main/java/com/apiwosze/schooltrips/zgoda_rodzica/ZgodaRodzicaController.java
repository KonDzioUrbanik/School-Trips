package com.apiwosze.schooltrips.zgoda_rodzica;

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
    public List<ZgodaRodzicaModel> getAllZgodaRodzica(){
        return zgodaRodzicaService.getAllZgodaRodzica();
    }
    @GetMapping("/{id}")
    public ZgodaRodzicaModel getZgodaRodzicaById(@PathVariable Long id){
        return zgodaRodzicaService.getZgodaRodzicaById(id);
    }
    @DeleteMapping("/{id}")
    public void deleteZgodaRodzica(@PathVariable Long id){
        zgodaRodzicaService.deleteZgodaRodzica(id);
    }
    @PutMapping("/{id}")
    public ZgodaRodzicaModel updateZgodaRodzica(@PathVariable Long id, @RequestBody ZgodaRodzicaDto zgodaRodzicaDto){
        return zgodaRodzicaService.updateZgodaRodzica(id, zgodaRodzicaDto);
    }
    @PostMapping
    public ZgodaRodzicaModel createZgodaRodzica(@RequestBody ZgodaRodzicaDto zgodaRodzicaDto){
        return zgodaRodzicaService.createZgodaRodzica(zgodaRodzicaDto);
    }
}
