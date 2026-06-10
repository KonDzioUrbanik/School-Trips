package com.apiwosze.schooltrips.uczestnictwo;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/uczestnictwo")
public class UczestnictwoController {
    private final UczestnictwoService uczestnictwoService;

    public UczestnictwoController(UczestnictwoService uczestnictwoService) {
        this.uczestnictwoService = uczestnictwoService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')")
    public UczestnictwoModel getUczestnictwoById(@PathVariable Long id){
        return uczestnictwoService.getUczestnictwoById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')")
    public List<UczestnictwoModel> getAllUczestnicy(){
        return uczestnictwoService.getAllUczestnicy();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')")
    public UczestnictwoModel createUczestnictwo(@RequestBody @Valid UczestnictwoDto uczestnictwoDto){
        return uczestnictwoService.createUczestnictwo(uczestnictwoDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public void deleteUczestnictwo(@PathVariable Long id){
        uczestnictwoService.deleteUczestnictwo(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')")
    public UczestnictwoModel updateUczestnictwo(@PathVariable Long id, @RequestBody @Valid UczestnictwoDto uczestnictwoDto){
        return uczestnictwoService.updateUczestnictwo(id, uczestnictwoDto);
    }

    @PostMapping("/{id}/oplac-zaliczke")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')")
    public UczestnictwoModel oplacZaliczke(@PathVariable Long id, 
                                           @RequestBody @Valid SimulatedPaymentDto paymentDto, 
                                           Principal principal) {
        return uczestnictwoService.oplacZaliczke(id, paymentDto, principal.getName());
    }
}
