package com.apiwosze.schooltrips.opiekun_wycieczki;

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
    public OpiekunWycieczkiModel getOpiekunById(@PathVariable Long id) {
        return opiekunWycieczkiService.getOpiekunWycieczkiById(id);
    }
    @DeleteMapping("/{id}")
    public void deleteOpiekun(@PathVariable Long id){
        opiekunWycieczkiService.deleteOpiekunWycieczki(id);
    }
    @PutMapping("/{id}")
    public OpiekunWycieczkiModel updateOpiekun(@PathVariable Long id, @RequestBody OpiekunWycieczkiDto opiekunWycieczkiDto){
        return opiekunWycieczkiService.updateOpiekunWycieczki(id, opiekunWycieczkiDto);
    }
    @GetMapping
    public List<OpiekunWycieczkiModel> getAllOpiekun(){
        return opiekunWycieczkiService.getAllOpiekunowie();
    }
    @PostMapping
    public OpiekunWycieczkiModel createOpiekun(@RequestBody OpiekunWycieczkiDto opiekunWycieczkiDto){
        return opiekunWycieczkiService.createOpiekunWycieczki(opiekunWycieczkiDto);
    }
}
