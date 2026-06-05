package com.apiwosze.schooltrips.security; // Definicja pakietu bezpieczeństwa

import org.springframework.context.annotation.Bean; // Import adnotacji Bean do definiowania obiektów zarządzanych przez Springa
import org.springframework.context.annotation.Configuration; // Import adnotacji oznaczającej klasę jako konfiguracyjną
import org.springframework.http.HttpMethod; // Import enuma reprezentującego metody HTTP (GET, POST, itp.)
import org.springframework.security.authentication.AuthenticationManager; // Import interfejsu zarządzającego procesem uwierzytelniania
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // Import klasy konfiguracji uwierzytelniania
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Import adnotacji aktywującej metodowe zabezpieczenia (np. @PreAuthorize)
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // Import klasy służącej do konfiguracji zabezpieczeń HTTP
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // Import adnotacji włączającej zabezpieczenia webowe Spring Security
import org.springframework.security.config.http.SessionCreationPolicy; // Import enuma określającego politykę zarządzania sesjami (np. STATELESS)
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Import silnego algorytmu haszowania haseł (BCrypt)
import org.springframework.security.crypto.password.PasswordEncoder; // Import interfejsu do kodowania i porównywania haseł
import org.springframework.security.web.SecurityFilterChain; // Import klasy reprezentującej łańcuch filtrów bezpieczeństwa
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Import klasy filtra standardowego uwierzytelniania

@Configuration // Oznacza klasę jako źródło definicji Beanów konfiguracyjnych dla aplikacji
@EnableWebSecurity // Włącza standardową konfigurację zabezpieczeń sieciowych Spring Security
@EnableMethodSecurity(prePostEnabled = true) // Włącza zabezpieczenia na poziomie metod w kontrolerach za pomocą adnotacji @PreAuthorize
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter; // Deklaracja referencji filtra JWT do przechwytywania tokenów
    private final CustomAuthenticationEntryPoint authenticationEntryPoint; // Punkt wejścia w przypadku braku tokenu (401)
    private final CustomAccessDeniedHandler accessDeniedHandler; // Handler wywoływany przy braku uprawnień (403)

    // Konstruktor wstrzykujący wymagane komponenty zależności
    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          CustomAuthenticationEntryPoint authenticationEntryPoint,
                          CustomAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    // Główny Bean definiujący łańcuch filtrów bezpieczeństwa (SecurityFilterChain)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Wyłączenie ochrony CSRF (Cross-Site Request Forgery) - w API stateless opartym na JWT nie jest ona wymagana
                .csrf(csrf -> csrf.disable())
                // Rejestracja dedykowanych handlerów błędów bezpieczeństwa (Custom 401 i 403)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint) // Ustawienie handlera błędu 401
                        .accessDeniedHandler(accessDeniedHandler) // Ustawienie handlera błędu 403
                )
                // Konfiguracja reguł autoryzacji adresów URL (endpointów)
                .authorizeHttpRequests(auth -> auth
                        // Zezwolenie każdemu na dostęp do strony głównej, plików statycznych oraz dokumentacji Swagger API i OpenAPI docs
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Zezwolenie każdemu na endpoint logowania w celu pobrania tokenu JWT
                        .requestMatchers("/api/auth/login").permitAll()
                        // Każde inne żądanie do aplikacji wymaga bycia uwierzytelnionym (posiadania ważnego tokenu JWT)
                        .anyRequest().authenticated()
                )
                // Zdefiniowanie polityki sesji jako bezstanowej (stateless) - serwer nie tworzy sesji HTTP na dysku/w pamięci
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Dodanie naszego własnego filtra JWT przed standardowym filtrem uwierzytelniania UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // Zwrócenie skonfigurowanego łańcucha filtrów
    }

    // Bean enkapsulujący koder haseł wykorzystujący algorytm BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Zwraca instancję BCryptPasswordEncoder (bezpieczne haszowanie)
    }

    // Bean udostępniający AuthenticationManager - wymagany przez AuthController do procesu logowania i weryfikacji poświadczeń
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager(); // Pobranie menedżera uwierzytelniania ze standardowej konfiguracji Springa
    }
}