package com.apiwosze.schooltrips.uczestnictwo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/uczestnictwo")
public class UczestnictwoController {
    private final UczestnictwoService uczestnictwoService;

    public UczestnictwoController(UczestnictwoService uczestnictwoService) {
        this.uczestnictwoService = uczestnictwoService;
    }

    @GetMapping("/{id}")
    public UczestnictwoModel getUczestnictwoById(@PathVariable Long id){
        return uczestnictwoService.getUczestnictwoById(id);
    }
    @GetMapping
    public List<UczestnictwoModel> getAllUczestnicy(){
        return uczestnictwoService.getAllUczestnicy();
    }
    @PostMapping
    public UczestnictwoModel createUczestnictwo(@RequestBody UczestnictwoDto uczestnictwoDto){
        return uczestnictwoService.createUczestnictwo(uczestnictwoDto);
    }
    @DeleteMapping("/{id}")
    public void deleteUczestnictwo(@PathVariable Long id){
        uczestnictwoService.deleteUczestnictwo(id);
    }
    @PutMapping("/{id}")
    public UczestnictwoModel updateUczestnictwo(@PathVariable Long id, @RequestBody UczestnictwoDto uczestnictwoDto){
        return uczestnictwoService.updateUczestnictwo(id, uczestnictwoDto);
    }
}
