package com.apiwosze.schooltrips.wycieczka;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WycieczkaService {
    private final WycieczkaRepository wycieczkaRepository;


    public WycieczkaService(WycieczkaRepository wycieczkaRepository) {
        this.wycieczkaRepository = wycieczkaRepository;
    }

    public WycieczkaModel createWycieczka(WycieczkaDto wycieczkaDto){
        WycieczkaModel wycieczkaModel = new WycieczkaModel();
        wycieczkaModel.setNazwa(wycieczkaDto.nazwa());
        wycieczkaModel.setData_rozpoczecia(wycieczkaDto.dataRozpoczecia());
        wycieczkaModel.setData_zakonczenia(wycieczkaDto.dataZakonczenia());
        wycieczkaModel.setMiejsce_docelowe(wycieczkaDto.miejsceDocelowe());
        wycieczkaModel.setKoszt_na_osobe(wycieczkaDto.kosztNaOsobe());
        wycieczkaModel.setStatus(wycieczkaDto.status());
        return wycieczkaRepository.save(wycieczkaModel);
    }
    public void deleteWycieczka(Long id){
        wycieczkaRepository.deleteById(id);
    }
    public WycieczkaModel updateWycieczka(Long id, WycieczkaDto wycieczkaDto){
        WycieczkaModel wycieczkaModelEdit = wycieczkaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiej wycieczki!"));
        wycieczkaModelEdit.setNazwa(wycieczkaDto.nazwa());
        wycieczkaModelEdit.setData_rozpoczecia(wycieczkaDto.dataRozpoczecia());
        wycieczkaModelEdit.setData_zakonczenia(wycieczkaDto.dataZakonczenia());
        wycieczkaModelEdit.setMiejsce_docelowe(wycieczkaDto.miejsceDocelowe());
        wycieczkaModelEdit.setKoszt_na_osobe(wycieczkaDto.kosztNaOsobe());
        wycieczkaModelEdit.setStatus(wycieczkaDto.status());
        return wycieczkaRepository.save(wycieczkaModelEdit);
    }
    public List<WycieczkaModel> getAllWycieczki(){
        return wycieczkaRepository.findAll();
    }
    public WycieczkaModel getWycieczkaById(Long id){
        return wycieczkaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiej wycieczki!"));
    }

}
