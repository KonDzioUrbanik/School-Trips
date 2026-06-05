package com.apiwosze.schooltrips.zgoda_rodzica; // Definicja pakietu dla zgód rodziców

import jakarta.validation.Valid; // Import adnotacji do walidacji danych wejściowych
import org.springframework.http.HttpHeaders; // Import klasy nagłówków HTTP
import org.springframework.http.MediaType; // Import klasy typów MIME (Content-Type)
import org.springframework.http.ResponseEntity; // Import klasy reprezentującej odpowiedź HTTP
import org.springframework.security.access.prepost.PreAuthorize; // Import adnotacji zabezpieczenia metod ról użytkowników
import org.springframework.web.bind.annotation.*; // Import adnotacji REST Spring MVC

import java.util.List; // Import listy Javy

@RestController // Kontroler REST zwracający JSON
@RequestMapping("/api/zgoda_rodzica") // Ścieżka bazowa dla kontrolera
public class ZgodaRodzicaController {
    
    private final ZgodaRodzicaService zgodaRodzicaService; // Serwis obsługi logiki biznesowej zgód
    private final PdfService pdfService; // Serwis generujący dokumenty PDF

    // Konstruktor wstrzykujący oba serwisy
    public ZgodaRodzicaController(ZgodaRodzicaService zgodaRodzicaService, PdfService pdfService) {
        this.zgodaRodzicaService = zgodaRodzicaService;
        this.pdfService = pdfService;
    }

    // Pobranie wszystkich zgód rodziców z bazy danych
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')") // Dostęp dla wszystkich zalogowanych ról
    public List<ZgodaRodzicaModel> getAllZgodaRodzica(){
        return zgodaRodzicaService.getAllZgodaRodzica();
    }

    // Pobranie zgody rodzica o określonym ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')") // Dostęp dla wszystkich zalogowanych ról
    public ZgodaRodzicaModel getZgodaRodzicaById(@PathVariable Long id){
        return zgodaRodzicaService.getZgodaRodzicaById(id);
    }

    // Endpoint generujący i zwracający plik PDF z wybraną zgodą rodzica
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')") // Dostęp dla wszystkich zalogowanych ról
    public ResponseEntity<byte[]> getZgodaRodzicaPdf(@PathVariable Long id) {
        // Pobranie szczegółowych danych zgody (rzuci NoSuchElementException jeśli nie istnieje)
        ZgodaRodzicaModel zgoda = zgodaRodzicaService.getZgodaRodzicaById(id);
        
        // Generowanie tablicy bajtów dokumentu PDF przy użyciu PdfService
        byte[] pdfBytes = pdfService.generateConsentPdf(zgoda);
        
        // Konstruowanie odpowiedzi HTTP zawierającej dane binarne PDF
        return ResponseEntity.ok()
                // Ustawienie nagłówka Content-Disposition na 'inline' w celu podglądu pliku bezpośrednio w przeglądarce zamiast wymuszania pobrania
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=zgoda_rodzica_" + id + ".pdf")
                // Ustawienie nagłówka Content-Type na 'application/pdf'
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes); // Wstrzyknięcie zawartości binarnej PDF do ciała odpowiedzi
    }

    // Usunięcie zgody rodzica o danym ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')") // Usunąć zgodę może tylko Admin lub Nauczyciel
    public void deleteZgodaRodzica(@PathVariable Long id){
        zgodaRodzicaService.deleteZgodaRodzica(id);
    }

    // Aktualizacja zgody rodzica o danym ID
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL')") // Edytować zgodę może tylko Admin lub Nauczyciel
    public ZgodaRodzicaModel updateZgodaRodzica(@PathVariable Long id, @RequestBody @Valid ZgodaRodzicaDto zgodaRodzicaDto){
        return zgodaRodzicaService.updateZgodaRodzica(id, zgodaRodzicaDto);
    }

    // Utworzenie nowej zgody rodzica w systemie
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NAUCZYCIEL', 'UCZEN_RODZIC')") // Zgodę może zarejestrować każdy zalogowany użytkownik
    public ZgodaRodzicaModel createZgodaRodzica(@RequestBody @Valid ZgodaRodzicaDto zgodaRodzicaDto){
        return zgodaRodzicaService.createZgodaRodzica(zgodaRodzicaDto);
    }
}
