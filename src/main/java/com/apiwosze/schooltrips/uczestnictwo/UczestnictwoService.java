package com.apiwosze.schooltrips.uczestnictwo;

import com.apiwosze.schooltrips.uczen.UczenModel;
import com.apiwosze.schooltrips.uczen.UczenRepository;
import com.apiwosze.schooltrips.wycieczka.WycieczkaModel;
import com.apiwosze.schooltrips.wycieczka.WycieczkaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UczestnictwoService {
    private final UczestnictwoRepository uczestnictwoRepository;
    private final UczenRepository uczenRepository;
    private final WycieczkaRepository wycieczkaRepository;

    public UczestnictwoService(UczestnictwoRepository uczestnictwoRepository, UczenRepository uczenRepository, WycieczkaRepository wycieczkaRepository) {
        this.uczestnictwoRepository = uczestnictwoRepository;
        this.uczenRepository = uczenRepository;
        this.wycieczkaRepository = wycieczkaRepository;
    }


    public UczestnictwoModel createUczestnictwo (UczestnictwoDto uczestnictwoDto){
        UczenModel uczenModel = uczenRepository.findById(uczestnictwoDto.uczenId())
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego ucznia!"));
        WycieczkaModel wycieczkaModel = wycieczkaRepository.findById(uczestnictwoDto.wycieczkaId())
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiej wycieczki!"));
        
        // Zapisy zamykają się na tydzień (7 dni) przed planowaną wycieczką
        if (LocalDate.now().plusDays(7).isAfter(wycieczkaModel.getData_rozpoczecia())) {
            throw new IllegalArgumentException("Zapisy na tę wycieczkę zostały zamknięte! Rejestracja zamyka się na 7 dni przed rozpoczęciem wycieczki.");
        }

        UczestnictwoModel uczestnictwoModel = new UczestnictwoModel();
        uczestnictwoModel.setUczen(uczenModel);
        uczestnictwoModel.setWycieczka(wycieczkaModel);
        uczestnictwoModel.setCzy_jedzie(uczestnictwoDto.czyJedzie());
        uczestnictwoModel.setUwagi(uczestnictwoDto.uwagi());
        uczestnictwoModel.setData_zapisania(LocalDate.now());
        return uczestnictwoRepository.save(uczestnictwoModel);
    }
    public void deleteUczestnictwo(Long id){
        uczestnictwoRepository.deleteById(id);
    }
    public UczestnictwoModel updateUczestnictwo(Long id, UczestnictwoDto uczestnictwoDto){
        UczestnictwoModel uczestnictwoModelEdit = uczestnictwoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego uczestnictwa!"));
        uczestnictwoModelEdit.setCzy_jedzie(uczestnictwoDto.czyJedzie());
        uczestnictwoModelEdit.setUwagi(uczestnictwoDto.uwagi());
        return uczestnictwoRepository.save(uczestnictwoModelEdit);
    }
    public UczestnictwoModel getUczestnictwoById(Long id){
        return uczestnictwoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego uczestnictwa!"));
    }
    public List<UczestnictwoModel> getAllUczestnicy(){
        return uczestnictwoRepository.findAll();
    }

    public UczestnictwoModel oplacZaliczke(Long id, SimulatedPaymentDto paymentDto, String username) {
        UczestnictwoModel uczestnictwo = uczestnictwoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego uczestnictwa!"));

        // Sprawdzamy, czy zalogowany użytkownik to uczeń i czy to jego własne zgłoszenie
        UczenModel uczenZalogowany = uczenRepository.findByUserUsername(username).orElse(null);
        if (uczenZalogowany != null && !uczestnictwo.getUczen().getId().equals(uczenZalogowany.getId())) {
            throw new IllegalArgumentException("Brak uprawnień! Możesz opłacić zaliczkę tylko za własne zgłoszenia.");
        }

        if (uczestnictwo.isZaliczkaOplacona()) {
            throw new IllegalArgumentException("Zaliczka na tę wycieczkę została już opłacona!");
        }

        // Symulacja odrzucenia karty dla numeru zaczynającego się od "4000"
        String cleanCardNumber = paymentDto.cardNumber().replace(" ", "").replace("-", "");
        if (cleanCardNumber.startsWith("4000")) {
            throw new IllegalArgumentException("Błąd Stripe (Symulacja): Płatność odrzucona przez bank (kod: card_declined). Spróbuj użyć innej karty (np. testowej Stripe: 4242 4242 4242 4242).");
        }

        uczestnictwo.setZaliczkaOplacona(true);
        uczestnictwo.setDataOplatyZaliczki(java.time.LocalDateTime.now());
        uczestnictwo.setStripePaymentIntentId("pi_sim_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 18));

        return uczestnictwoRepository.save(uczestnictwo);
    }
}
