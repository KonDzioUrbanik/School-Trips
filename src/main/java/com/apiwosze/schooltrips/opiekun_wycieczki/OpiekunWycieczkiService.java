package com.apiwosze.schooltrips.opiekun_wycieczki;

import com.apiwosze.schooltrips.nauczyciel.NauczycielRepository;
import com.apiwosze.schooltrips.wycieczka.WycieczkaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpiekunWycieczkiService {
    private final OpiekunWycieczkiRepository opiekunWycieczkiRepository;
    private final WycieczkaRepository wycieczkaRepository;
    private final NauczycielRepository nauczycielRepository;

    public OpiekunWycieczkiService(OpiekunWycieczkiRepository opiekunWycieczkiRepository, WycieczkaRepository wycieczkaRepository, NauczycielRepository nauczycielRepository) {
        this.opiekunWycieczkiRepository = opiekunWycieczkiRepository;

        this.wycieczkaRepository = wycieczkaRepository;
        this.nauczycielRepository = nauczycielRepository;
    }

    public OpiekunWycieczkiModel createOpiekunWycieczki(OpiekunWycieczkiDto opiekunWycieczkiDto) {
        OpiekunWycieczkiModel opiekunWycieczkiModel = new OpiekunWycieczkiModel();
        opiekunWycieczkiModel.setRola(opiekunWycieczkiDto.rola());
        opiekunWycieczkiModel.setWycieczkaOpiekun(wycieczkaRepository.findById(opiekunWycieczkiDto.wycieczkaId())
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiej wycieczki!")));
        opiekunWycieczkiModel.setNauczyciel(nauczycielRepository.findById(opiekunWycieczkiDto.nauczycielId())
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego nauczyciela!")));
        return opiekunWycieczkiRepository.save(opiekunWycieczkiModel);
    }

    public void deleteOpiekunWycieczki(Long id){
        opiekunWycieczkiRepository.deleteById(id);
    }
    public OpiekunWycieczkiModel updateOpiekunWycieczki(Long id, OpiekunWycieczkiDto opiekunWycieczkiDto){
        OpiekunWycieczkiModel opiekunWycieczkiModelEdit = opiekunWycieczkiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego opiekuna wycieczki!"));
        if(!opiekunWycieczkiModelEdit.getNauczyciel().getId().equals(opiekunWycieczkiDto.nauczycielId())){
            opiekunWycieczkiModelEdit.setNauczyciel(nauczycielRepository.findById(opiekunWycieczkiDto.nauczycielId())
                    .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego nauczyciela!")));
        }
        opiekunWycieczkiModelEdit.setRola(opiekunWycieczkiDto.rola());
        return opiekunWycieczkiRepository.save(opiekunWycieczkiModelEdit);
    }
    public OpiekunWycieczkiModel getOpiekunWycieczkiById(Long id){
        return opiekunWycieczkiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego opiekuna wycieczki!"));
    }
    public List<OpiekunWycieczkiModel> getAllOpiekunowie(){
        return opiekunWycieczkiRepository.findAll();
    }
}
