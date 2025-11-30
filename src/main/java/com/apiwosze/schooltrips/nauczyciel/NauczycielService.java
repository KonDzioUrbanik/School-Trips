package com.apiwosze.schooltrips.nauczyciel;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NauczycielService {
    private final NauczycielRepository nauczycielRepository;

    public NauczycielService(NauczycielRepository nauczycielRepository) {
        this.nauczycielRepository = nauczycielRepository;
    }

    public NauczycielModel createNauczyciel(NauczycielDto nauczycielDto) {
        if (nauczycielRepository.existsByImieAndNazwisko(nauczycielDto.imie(), nauczycielDto.nazwisko())) {
            throw new IllegalArgumentException("Nauczyciel już istnieje!");
        } else {
            NauczycielModel nauczycielModel = new NauczycielModel();
            nauczycielModel.setImie(nauczycielDto.imie());
            nauczycielModel.setNazwisko(nauczycielDto.nazwisko());
            nauczycielModel.setPrzedmiot(nauczycielDto.przedmiot());
            nauczycielModel.setTelefon_kontaktowy(nauczycielDto.telefon_kontaktowy());
            return nauczycielRepository.save(nauczycielModel);
        }
    }

    @Transactional
    public void deleteNauczyciel(NauczycielDto nauczycielDto) {
        if (!nauczycielRepository.existsByImieAndNazwisko(nauczycielDto.imie(), nauczycielDto.nazwisko())) {
            throw new IllegalArgumentException("Nie ma takiego nauczyciela!");
        } else {
            nauczycielRepository.deleteByImieAndNazwisko(nauczycielDto.imie(), nauczycielDto.nazwisko());
        }
    }

    public List<NauczycielModel> getAllNauczyciele() {
        return nauczycielRepository.findAll();
    }
    public NauczycielModel updatePrzedmiotAndTelefonNauczyciel(NauczycielDto nauczycielDto){
        NauczycielModel nauczycielModelEdit = nauczycielRepository.findByImieAndNazwisko(nauczycielDto.imie(), nauczycielDto.nazwisko())
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego nauczyciela!"));
        nauczycielModelEdit.setPrzedmiot(nauczycielDto.przedmiot());
        nauczycielModelEdit.setTelefon_kontaktowy(nauczycielDto.telefon_kontaktowy());
        return nauczycielRepository.save(nauczycielModelEdit);
    }
    public NauczycielModel updateImieAndNazwiskoNauczyciela(Long id,NauczycielDto nauczycielDto){
        NauczycielModel nauczycielModelEdit = nauczycielRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego nauczyciela!"));
        nauczycielModelEdit.setImie(nauczycielDto.imie());
        nauczycielModelEdit.setNazwisko(nauczycielDto.nazwisko());
        return nauczycielRepository.save(nauczycielModelEdit);
    }


}
