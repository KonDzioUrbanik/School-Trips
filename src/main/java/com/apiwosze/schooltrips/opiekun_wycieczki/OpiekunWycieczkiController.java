package com.apiwosze.schooltrips.opiekun_wycieczki; // Definicja pakietu opiekunów wycieczki

import jakarta.validation.Valid; // Import adnotacji walidującej parametry wejściowe
import org.springframework.security.access.prepost.PreAuthorize; // Import adnotacji autoryzacji ról użytkownika
import org.springframework.web.bind.annotation.*; // Import adnotacji kontrolera REST Spring MVC

import java.util.List; // Import klasy listy Javy

@RestController // Oznaczenie klasy jako kontrolera REST (zwracającego odpowiedzi JSON)
@RequestMapping("/api/opiekun_wycieczki") // Bazowa ścieżka URI dla endpointów opiekunów
public class OpiekunWycieczkiController {
    
    // Prywatne pole serwisu opiekunów wycieczki
    final private OpiekunWycieczkiService opiekunWycieczkiService;

    // Konstruktor wstrzykujący zależność serwisu opiekunów
    public OpiekunWycieczkiController(OpiekunWycieczkiService opiekunWycieczkiService) {
        this.opiekunWycieczkiService = opiekunWycieczkiService;
    }

    // Endpoint pobierający opiekuna na podstawie unikalnego identyfikatora ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')") // Dostęp dla wszystkich zalogowanych użytkowników
    public OpiekunWycieczkiModel getOpiekunById(@PathVariable Long id) {
        return opiekunWycieczkiService.getOpiekunWycieczkiById(id); // Wywołanie metody serwisu i zwrócenie wyniku
    }

    // Endpoint usuwający opiekuna o danym ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')") // Dostęp zastrzeżony dla roli Admin oraz Nauczyciel
    public void deleteOpiekun(@PathVariable Long id){
        opiekunWycieczkiService.deleteOpiekunWycieczki(id); // Wywołanie logiki usunięcia z serwisu
    }

    // Endpoint aktualizujący dane opiekuna o podanym ID na podstawie obiektu DTO
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')") // Dostęp zastrzeżony dla roli Admin oraz Nauczyciel
    public OpiekunWycieczkiModel updateOpiekun(@PathVariable Long id, @RequestBody @Valid OpiekunWycieczkiDto opiekunWycieczkiDto){
        return opiekunWycieczkiService.updateOpiekunWycieczki(id, opiekunWycieczkiDto); // Wywołanie aktualizacji w serwisie
    }

    // Endpoint pobierający listę wszystkich opiekunów wycieczek w systemie
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')") // Dostęp dla wszystkich zalogowanych użytkowników
    public List<OpiekunWycieczkiModel> getAllOpiekun(){
        return opiekunWycieczkiService.getAllOpiekunowie(); // Wywołanie metody serwisu pobierającej wszystkich opiekunów
    }

    // Endpoint dodający nowego opiekuna wycieczki na podstawie obiektu DTO
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')") // Dostęp zastrzeżony dla roli Admin oraz Nauczyciel
    public OpiekunWycieczkiModel createOpiekun(@RequestBody @Valid OpiekunWycieczkiDto opiekunWycieczkiDto){
        return opiekunWycieczkiService.createOpiekunWycieczki(opiekunWycieczkiDto); // Utworzenie i zapisanie opiekuna w serwisie
    }
}
