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
}
