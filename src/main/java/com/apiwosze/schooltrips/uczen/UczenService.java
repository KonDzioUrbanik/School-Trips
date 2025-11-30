package com.apiwosze.schooltrips.uczen;

import com.apiwosze.schooltrips.klasa.KlasaModel;
import com.apiwosze.schooltrips.klasa.KlasaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UczenService {
    private final UczenRepository uczenRepository;
    private final KlasaRepository klasaRepository;

    public UczenService(UczenRepository uczenRepository, KlasaRepository klasaRepository) {
        this.uczenRepository = uczenRepository;
        this.klasaRepository = klasaRepository;
    }


    public UczenModel createUczen(UczenDto uczenDto) {
        KlasaModel klasa = klasaRepository.findById(uczenDto.klasaId())
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiej klasy!"));
        UczenModel uczenModel = new UczenModel();
        uczenModel.setImie(uczenDto.imie());
        uczenModel.setNazwisko(uczenDto.nazwisko());
        uczenModel.setData_urodzenia(uczenDto.data_urodzenia());
        uczenModel.setKlasa(klasa);
        return uczenRepository.save(uczenModel);
    }
    public void deleteUczen(Long id){
        uczenRepository.deleteById(id);
    }
    public UczenModel updateUczen(Long id, UczenDto uczenDto){
        UczenModel uczenModelEdit = uczenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego ucznia!"));
        uczenModelEdit.setImie(uczenDto.imie());
        uczenModelEdit.setNazwisko(uczenDto.nazwisko());
        uczenModelEdit.setData_urodzenia(uczenDto.data_urodzenia());
        if (!uczenModelEdit.getKlasa().getId().equals(uczenDto.klasaId())){
            uczenModelEdit.setKlasa(klasaRepository.findById(uczenDto.klasaId())
                    .orElseThrow(() -> new IllegalArgumentException("Nie ma takiej klasy!")));
        }
        return uczenRepository.save(uczenModelEdit);
    }
    public UczenModel getUczenById(Long id){
        return uczenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego ucznia!"));
    }
    public List<UczenModel> getAllUczen(){
        return uczenRepository.findAll();
    }
}
