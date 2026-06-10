package com.apiwosze.schooltrips.uczestnictwo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SimulatedPaymentDto(
        @NotBlank(message = "Adres email jest wymagany")
        @Email(message = "Niepoprawny format adresu email")
        String email,

        @NotBlank(message = "Imię i nazwisko właściciela karty są wymagane")
        String nameOnCard,

        @NotBlank(message = "Numer karty jest wymagany")
        @Pattern(regexp = "^(?:[0-9]{4}[-\\s]?){3}[0-9]{4}$", message = "Niepoprawny numer karty (wymagane 16 cyfr)")
        String cardNumber,

        @NotBlank(message = "Data ważności karty jest wymagana")
        @Pattern(regexp = "^(0[1-9]|1[0-2])\\/?([0-9]{2})$", message = "Niepoprawny format daty ważności (MM/YY)")
        String expiryDate,

        @NotBlank(message = "Kod CVC jest wymagany")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "Niepoprawny kod CVC (wymagane 3 lub 4 cyfry)")
        String cvc
) {
}
