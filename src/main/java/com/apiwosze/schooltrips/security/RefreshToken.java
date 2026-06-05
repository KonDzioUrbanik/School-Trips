package com.apiwosze.schooltrips.security; // Definicja pakietu bezpieczeństwa

import jakarta.persistence.*; // Import adnotacji mapowania JPA
import lombok.Data; // Import Lombok @Data dla automatycznych metod get/set
import java.time.Instant; // Import klasy Instant reprezentującej dokładny moment na osi czasu UTC

@Data // Generowanie getterów, setterów, toString przez Lombok
@Entity // Oznaczenie jako encja bazy danych
@Table(name = "refresh_token") // Nazwa tabeli w bazie przechowującej tokeny odświeżania
public class RefreshToken {

    @Id // Klucz główny
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-inkrementacja pola id przez bazę danych
    private Long id; // Identyfikator rekordu

    @Column(nullable = false, unique = true) // Token nie może być pusty i musi być unikalny
    private String token; // Unikalna losowa wartość UUID reprezentująca token odświeżania

    @Column(nullable = false) // Data wygaśnięcia nie może być pusta
    private Instant expiryDate; // Moment czasu, po którym token traci ważność

    @OneToOne // Relacja jeden-do-jednego: dany token odświeżania przypisany jest do dokładnie jednego użytkownika
    @JoinColumn(name = "user_id", referencedColumnName = "id") // Klucz obcy wskazujący na tabelę uzytkownik
    private Uzytkownik user; // Referencja do encji użytkownika
}
