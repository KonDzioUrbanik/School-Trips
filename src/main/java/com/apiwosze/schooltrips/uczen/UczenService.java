package com.apiwosze.schooltrips.uczen;

import com.apiwosze.schooltrips.klasa.KlasaModel;
import com.apiwosze.schooltrips.klasa.KlasaRepository;
import com.apiwosze.schooltrips.security.Uzytkownik;
import com.apiwosze.schooltrips.security.RolaUzytkownika;
import com.apiwosze.schooltrips.security.UzytkownikRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UczenService {
    private final UczenRepository uczenRepository;
    private final KlasaRepository klasaRepository;
    private final UzytkownikRepository uzytkownikRepository;
    private final PasswordEncoder passwordEncoder;

    public UczenService(UczenRepository uczenRepository, KlasaRepository klasaRepository, 
                        UzytkownikRepository uzytkownikRepository, PasswordEncoder passwordEncoder) {
        this.uczenRepository = uczenRepository;
        this.klasaRepository = klasaRepository;
        this.uzytkownikRepository = uzytkownikRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public UczenModel createUczen(UczenDto uczenDto) {
        KlasaModel klasa = klasaRepository.findById(uczenDto.klasaId())
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiej klasy!"));
        
        // Generowanie unikalnego loginu dla ucznia na podstawie imienia i nazwiska
        String baseUsername = (uczenDto.imie() + "." + uczenDto.nazwisko()).toLowerCase()
                .replace('ą', 'a').replace('ć', 'c').replace('ę', 'e')
                .replace('ł', 'l').replace('ń', 'n').replace('ó', 'o')
                .replace('ś', 's').replace('ź', 'z').replace('ż', 'z')
                .replaceAll("[^a-z0-9.]", "");
        String username = baseUsername;
        int counter = 1;
        while (uzytkownikRepository.findByUsername(username).isPresent()) {
            username = baseUsername + counter;
            counter++;
        }

        // Utworzenie powiązanego konta użytkownika (Uzytkownik)
        Uzytkownik user = new Uzytkownik();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("uczen123")); // Domyślne hasło
        user.setRola(RolaUzytkownika.UCZEN_RODZIC);

        UczenModel uczenModel = new UczenModel();
        uczenModel.setImie(uczenDto.imie());
        uczenModel.setNazwisko(uczenDto.nazwisko());
        uczenModel.setData_urodzenia(uczenDto.data_urodzenia());
        uczenModel.setKlasa(klasa);
        uczenModel.setUser(user); // Powiązanie konta
        
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
    public UczenModel getUczenByUsername(String username) {
        return uczenRepository.findByUserUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono profilu ucznia dla użytkownika: " + username));
    }
}
