package com.apiwosze.schooltrips.uczen;

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
    public UczenModel getUczenById(@PathVariable Long id){
        return uczenService.getUczenById(id);
    }
    @GetMapping
    public List<UczenModel> getAllUczen(){
        return uczenService.getAllUczen();
    }
    @PostMapping
    public UczenModel createUczen(@RequestBody UczenDto uczenDto){
        return uczenService.createUczen(uczenDto);
    }
    @DeleteMapping("/{id}")
    public void deleteUczen(@PathVariable Long id){
        uczenService.deleteUczen(id);
    }
    @PutMapping("/{id}")
    public UczenModel updateUczen(@PathVariable Long id, @RequestBody UczenDto uczenDto){
        return uczenService.updateUczen(id, uczenDto);
    }

}
