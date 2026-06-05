package com.apiwosze.schooltrips.aspect; // Definicja pakietu dla aspektów AOP

import org.aspectj.lang.ProceedingJoinPoint; // Import klasy reprezentującej punkt przecięcia (metodę, którą przechwytujemy)
import org.aspectj.lang.annotation.Around; // Import adnotacji określającej poradę typu "wokół" (around advice)
import org.aspectj.lang.annotation.Aspect; // Import adnotacji definiującej klasę jako Aspekt
import org.slf4j.Logger; // Import interfejsu logera
import org.slf4j.LoggerFactory; // Import fabryki logerów SLF4J
import org.springframework.security.core.Authentication; // Import reprezentacji zalogowanego użytkownika
import org.springframework.security.core.context.SecurityContextHolder; // Import kontekstu bezpieczeństwa Spring Security
import org.springframework.stereotype.Component; // Import adnotacji Spring Component

import java.util.Arrays; // Import klasy narzędziowej tablic

@Aspect // Oznaczenie klasy jako aspektu AOP
@Component // Rejestracja aspektu jako komponentu w kontenerze Spring
public class LoggingAspect {

    // Tworzenie loggera SLF4J przypisanego do klasy LoggingAspect
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    // Porada @Around pozwala przejąć kontrolę nad wykonaniem metody przed jej rozpoczęciem, w trakcie oraz po zakończeniu
    // Punkt przecięcia (pointcut) wskazuje na wszystkie metody w klasach oznaczonych adnotacją @RestController
    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        
        // 1. Pobranie nazwy wywoływanej metody i klasy
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        
        // 2. Pobranie parametrów wejściowych przesłanych do metody
        String args = Arrays.toString(joinPoint.getArgs());
        
        // 3. Pobranie nazwy zalogowanego użytkownika z kontekstu Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = "Gość"; // Wartość domyślna dla użytkowników niezalogowanych
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            currentUser = authentication.getName(); // Wyciągnięcie nazwy użytkownika
        }

        // Logowanie wejścia do metody
        log.info("[API Request] Użytkownik: '{}' | Wywołanie: {}.{}() z parametrami: {}", 
                currentUser, className, methodName, args);

        long start = System.currentTimeMillis(); // Zapisanie czasu rozpoczęcia wykonywania metody

        Object result;
        try {
            // Kontynuacja wykonywania przechwyconej metody (wywołanie właściwego kodu kontrolera)
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            // W razie wyjątku rzuconego przez metodę, logujemy błąd i rzucamy wyjątek dalej
            long executionTime = System.currentTimeMillis() - start;
            log.error("[API Error] Użytkownik: '{}' | Wywołanie: {}.{}() rzuciło błąd: '{}' po {} ms", 
                    currentUser, className, methodName, throwable.getMessage(), executionTime);
            throw throwable; // Ponowne rzucenie wyjątku w celu przetworzenia przez GlobalExceptionHandler
        }

        // Logowanie pomyślnego wyjścia z metody wraz z czasem wykonania w milisekundach
        long executionTime = System.currentTimeMillis() - start;
        log.info("[API Response] Użytkownik: '{}' | Wywołanie: {}.{}() zakończone sukcesem w {} ms", 
                currentUser, className, methodName, executionTime);

        return result; // Zwrócenie wyniku metody
    }
}
