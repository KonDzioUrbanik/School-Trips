package com.apiwosze.schooltrips.wycieczka;

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
    public List<WycieczkaModel> getAllWycieczki(){
        return wycieczkaService.getAllWycieczki();
    }
    @GetMapping("/{id}")
    public WycieczkaModel getWycieczkaById(@PathVariable Long id){
        return wycieczkaService.getWycieczkaById(id);
    }
    @DeleteMapping("/{id}")
    public void deleteWycieczka(@PathVariable Long id){
        wycieczkaService.deleteWycieczka(id);
    }
    @PutMapping("/{id}")
    public WycieczkaModel updateWycieczka(@PathVariable Long id, @RequestBody WycieczkaDto wycieczkaDto){
        return wycieczkaService.updateWycieczka(id, wycieczkaDto);
    }
    @PostMapping
    public WycieczkaModel createWycieczka(@RequestBody WycieczkaDto wycieczkaDto){
        return wycieczkaService.createWycieczka(wycieczkaDto);
    }

}
