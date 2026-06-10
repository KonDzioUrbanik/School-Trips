package com.apiwosze.schooltrips.zgoda_rodzica;

import com.apiwosze.schooltrips.klasa.KlasaModel;
import com.apiwosze.schooltrips.nauczyciel.NauczycielModel;
import com.apiwosze.schooltrips.opiekun_wycieczki.OpiekunWycieczkiModel;
import com.apiwosze.schooltrips.opiekun_wycieczki.Rola;
import com.apiwosze.schooltrips.uczestnictwo.UczestnictwoModel;
import com.apiwosze.schooltrips.uczen.UczenModel;
import com.apiwosze.schooltrips.wycieczka.WycieczkaModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfServiceTest {

    private PdfService pdfService;

    @BeforeEach
    void setUp() {
        pdfService = new PdfService();
    }

    @Test
    void testGenerateTripParticipantsPdf_Success() {
        // Given
        WycieczkaModel wycieczka = new WycieczkaModel();
        wycieczka.setId(1L);
        wycieczka.setNazwa("Zielona Szkoła w Bieszczadach");
        wycieczka.setMiejsce_docelowe("Ustrzyki Dolne");
        wycieczka.setData_rozpoczecia(LocalDate.now().plusDays(10));
        wycieczka.setData_zakonczenia(LocalDate.now().plusDays(15));
        wycieczka.setKoszt_na_osobe(new BigDecimal("500.0"));

        // Nauczyciel i Opiekun
        NauczycielModel nauczyciel = new NauczycielModel();
        nauczyciel.setId(1L);
        nauczyciel.setImie("Jan");
        nauczyciel.setNazwisko("Kowalski");
        nauczyciel.setTelefon_kontaktowy("123456789");

        OpiekunWycieczkiModel opiekun = new OpiekunWycieczkiModel();
        opiekun.setId(1L);
        opiekun.setNauczyciel(nauczyciel);
        opiekun.setRola(Rola.DOWODZĄCY);
        opiekun.setWycieczkaOpiekun(wycieczka);

        List<OpiekunWycieczkiModel> opiekunowieList = new ArrayList<>();
        opiekunowieList.add(opiekun);
        wycieczka.setOpiekunowie(opiekunowieList);

        // Klasa i Uczeń
        KlasaModel klasa = new KlasaModel();
        klasa.setId(1L);
        klasa.setNazwa("3A");
        klasa.setProfil("Mat-Fiz");

        UczenModel uczen = new UczenModel();
        uczen.setId(1L);
        uczen.setImie("Kamil");
        uczen.setNazwisko("Nowak");
        uczen.setKlasa(klasa);

        // Uczestnictwo
        UczestnictwoModel uczestnictwo = new UczestnictwoModel();
        uczestnictwo.setId(1L);
        uczestnictwo.setUczen(uczen);
        uczestnictwo.setWycieczka(wycieczka);
        uczestnictwo.setCzyJedzie(true);
        uczestnictwo.setUwagi("Dieta wegetariańska");
        uczestnictwo.setData_zapisania(LocalDate.now());

        List<UczestnictwoModel> uczestnictwoList = new ArrayList<>();
        uczestnictwoList.add(uczestnictwo);
        wycieczka.setUczestniczenie(uczestnictwoList);

        // When
        byte[] pdfBytes = pdfService.generateTripParticipantsPdf(wycieczka);

        // Then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
}
