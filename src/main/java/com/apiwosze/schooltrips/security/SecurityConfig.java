package com.apiwosze.schooltrips.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Wyłączamy CSRF na potrzeby nauki i testów z Postmana
                .authorizeHttpRequests(auth -> auth
                        // 1. Zezwolenie na dostęp do plików statycznych frontendu i Swaggera
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 2. KLASY
                        .requestMatchers(HttpMethod.GET, "/api/klasy/**").hasAnyRole("ADMIN", "NAUCZYCIEL")
                        .requestMatchers("/api/klasy/**").hasRole("ADMIN")

                        // 3. NAUCZYCIEL i OPIEKUN
                        .requestMatchers(HttpMethod.GET, "/api/nauczyciel/**", "/api/opiekun_wycieczki/**").hasAnyRole("ADMIN", "NAUCZYCIEL")
                        .requestMatchers("/api/nauczyciel/**", "/api/opiekun_wycieczki/**").hasRole("ADMIN")

                        // 4. UCZEŃ
                        .requestMatchers(HttpMethod.GET, "/api/uczen/**").hasAnyRole("ADMIN", "NAUCZYCIEL", "UCZEN_RODZIC")
                        .requestMatchers("/api/uczen/**").hasAnyRole("ADMIN", "NAUCZYCIEL")

                        // 5. WYCIECZKA
                        .requestMatchers(HttpMethod.GET, "/api/wycieczka/**").hasAnyRole("ADMIN", "NAUCZYCIEL", "UCZEN_RODZIC")
                        .requestMatchers("/api/wycieczka/**").hasAnyRole("ADMIN", "NAUCZYCIEL")

                        // 6. UCZESTNICTWO & ZGODY
                        .requestMatchers(HttpMethod.POST, "/api/uczestnictwo/**", "/api/zgoda_rodzica/**").hasAnyRole("ADMIN", "NAUCZYCIEL", "UCZEN_RODZIC")
                        .requestMatchers(HttpMethod.GET, "/api/uczestnictwo/**", "/api/zgoda_rodzica/**").hasAnyRole("ADMIN", "NAUCZYCIEL", "UCZEN_RODZIC")
                        .requestMatchers("/api/uczestnictwo/**", "/api/zgoda_rodzica/**").hasAnyRole("ADMIN", "NAUCZYCIEL")

                        // Reszta wymaga jakiegokolwiek logowania
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults()) // Zezwala na użycie w Postman (Basic Auth)
                .formLogin(Customizer.withDefaults()); // Włącza domyślny formularz logowania dla przeglądarki

        return http.build();
    }

    // Haszowanie haseł - nikt w bazie nie powinien widzieć hasła czystym tekstem
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}