package com.apiwosze.schooltrips.uczestnictwo;

import com.apiwosze.schooltrips.uczen.UczenModel;
import com.apiwosze.schooltrips.uczen.UczenRepository;
import com.apiwosze.schooltrips.wycieczka.WycieczkaModel;
import com.apiwosze.schooltrips.wycieczka.WycieczkaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UczestnictwoServiceTest {

    private UczestnictwoService uczestnictwoService;

    @Mock
    private UczestnictwoRepository uczestnictwoRepository;

    @Mock
    private UczenRepository uczenRepository;

    @Mock
    private WycieczkaRepository wycieczkaRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        uczestnictwoService = new UczestnictwoService(uczestnictwoRepository, uczenRepository, wycieczkaRepository);
    }

    @Test
    void testCreateUczestnictwo_Success_MoreThan7Days() {
        // Given
        Long uczenId = 1L;
        Long wycieczkaId = 2L;
        LocalDate tripStartDate = LocalDate.now().plusDays(8);

        UczenModel uczen = new UczenModel();
        uczen.setId(uczenId);

        WycieczkaModel wycieczka = new WycieczkaModel();
        wycieczka.setId(wycieczkaId);
        wycieczka.setData_rozpoczecia(tripStartDate);

        UczestnictwoDto dto = new UczestnictwoDto(uczenId, wycieczkaId, true, "Brak");

        when(uczenRepository.findById(uczenId)).thenReturn(Optional.of(uczen));
        when(wycieczkaRepository.findById(wycieczkaId)).thenReturn(Optional.of(wycieczka));
        when(uczestnictwoRepository.save(any(UczestnictwoModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UczestnictwoModel result = uczestnictwoService.createUczestnictwo(dto);

        // Then
        assertNotNull(result);
        assertEquals(uczen, result.getUczen());
        assertEquals(wycieczka, result.getWycieczka());
        assertTrue(result.isCzy_jedzie());
        assertEquals("Brak", result.getUwagi());
        assertEquals(LocalDate.now(), result.getData_zapisania());

        verify(uczestnictwoRepository, times(1)).save(any(UczestnictwoModel.class));
    }

    @Test
    void testCreateUczestnictwo_ThrowsException_LessThan7Days() {
        // Given
        Long uczenId = 1L;
        Long wycieczkaId = 2L;
        LocalDate tripStartDate = LocalDate.now().plusDays(6);

        UczenModel uczen = new UczenModel();
        uczen.setId(uczenId);

        WycieczkaModel wycieczka = new WycieczkaModel();
        wycieczka.setId(wycieczkaId);
        wycieczka.setData_rozpoczecia(tripStartDate);

        UczestnictwoDto dto = new UczestnictwoDto(uczenId, wycieczkaId, true, "Brak");

        when(uczenRepository.findById(uczenId)).thenReturn(Optional.of(uczen));
        when(wycieczkaRepository.findById(wycieczkaId)).thenReturn(Optional.of(wycieczka));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            uczestnictwoService.createUczestnictwo(dto);
        });

        assertTrue(exception.getMessage().contains("Zapisy na tę wycieczkę zostały zamknięte"));
        verify(uczestnictwoRepository, never()).save(any(UczestnictwoModel.class));
    }
}
