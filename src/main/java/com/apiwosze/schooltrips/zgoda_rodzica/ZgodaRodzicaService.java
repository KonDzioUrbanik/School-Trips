package com.apiwosze.schooltrips.zgoda_rodzica;

import com.apiwosze.schooltrips.uczestnictwo.UczestnictwoModel;
import com.apiwosze.schooltrips.uczestnictwo.UczestnictwoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZgodaRodzicaService {
    private final ZgodaRodzicaRepository zgodaRodzicaRepository;
    private final UczestnictwoRepository uczestnictwoRepository;

    public ZgodaRodzicaService(ZgodaRodzicaRepository zgodaRodzicaRepository, UczestnictwoRepository uczestnictwoRepository) {
        this.zgodaRodzicaRepository = zgodaRodzicaRepository;
        this.uczestnictwoRepository = uczestnictwoRepository;
    }
    public ZgodaRodzicaModel createZgodaRodzica(ZgodaRodzicaDto zgodaRodzicaDto){
        UczestnictwoModel uczestnictwoModel = uczestnictwoRepository.findById(zgodaRodzicaDto.uczestnictwoId())
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego uczestnictwa!"));
        ZgodaRodzicaModel zgodaRodzicaModel = new ZgodaRodzicaModel();
        zgodaRodzicaModel.setData_podpisania(zgodaRodzicaDto.dataPodpisania());
        zgodaRodzicaModel.setForma(zgodaRodzicaDto.forma());
        zgodaRodzicaModel.setCzy_dostarczona(zgodaRodzicaDto.czyDostarczona());
        zgodaRodzicaModel.setUczestnictwo(uczestnictwoModel);
        return zgodaRodzicaRepository.save(zgodaRodzicaModel);
    }
    public void deleteZgodaRodzica(Long id){
        zgodaRodzicaRepository.deleteById(id);
    }
    public ZgodaRodzicaModel updateZgodaRodzica(Long id, ZgodaRodzicaDto zgodaRodzicaDto){
        ZgodaRodzicaModel zgodaRodzicaModelEdit = zgodaRodzicaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiej zgody rodzica!"));
        zgodaRodzicaModelEdit.setData_podpisania(zgodaRodzicaDto.dataPodpisania());
        zgodaRodzicaModelEdit.setForma(zgodaRodzicaDto.forma());
        zgodaRodzicaModelEdit.setCzy_dostarczona(zgodaRodzicaDto.czyDostarczona());
        return zgodaRodzicaRepository.save(zgodaRodzicaModelEdit);
    }
    public ZgodaRodzicaModel getZgodaRodzicaById(Long id){
        return zgodaRodzicaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiej zgody rodzica!"));
    }
    public List<ZgodaRodzicaModel> getAllZgodaRodzica(){
        return zgodaRodzicaRepository.findAll();
    }
}
