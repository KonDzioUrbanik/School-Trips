package com.apiwosze.schooltrips.zgoda_rodzica; // Definicja pakietu dla zgód rodziców

import com.apiwosze.schooltrips.uczen.UczenModel; // Import encji ucznia
import com.apiwosze.schooltrips.wycieczka.WycieczkaModel; // Import encji wycieczki
import com.apiwosze.schooltrips.opiekun_wycieczki.OpiekunWycieczkiModel; // Import encji opiekunów
import com.apiwosze.schooltrips.uczestnictwo.UczestnictwoModel; // Import encji uczestnictwa
import com.lowagie.text.*; // Import klas biblioteki OpenPDF do tworzenia dokumentu, akapitów i czcionek
import com.lowagie.text.alignment.HorizontalAlignment; // Import enuma wyrównania horyzontalnego OpenPDF
import com.lowagie.text.pdf.PdfWriter; // Import klasy generatora PDF
import org.springframework.stereotype.Service; // Import adnotacji Spring Service

import java.io.ByteArrayOutputStream; // Import strumienia wyjściowego bajtów (zapis PDF w pamięci RAM)
import java.math.BigDecimal; // Import klasy do obsługi cen

@Service // Rejestracja jako serwis w kontenerze Springa
public class PdfService {

    // Metoda generująca dokument PDF ze zgodą rodzica i zwracająca go jako tablicę
    // bajtów (byte[])
    public byte[] generateConsentPdf(ZgodaRodzicaModel zgoda) {

        // Pobranie danych o uczestnictwie, uczniu oraz wycieczce powiązanych z tą zgodą
        var uczestnictwo = zgoda.getUczestnictwo();
        UczenModel uczen = uczestnictwo.getUczen();
        WycieczkaModel wycieczka = uczestnictwo.getWycieczka();

        // Stworzenie obiektu dokumentu formatu A4 z marginesami 50 jednostek
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream(); // Strumień do zapisu binarnego PDF w pamięci

        try {
            // Skojarzenie dokumentu ze strumieniem wyjściowym za pomocą PdfWriter
            PdfWriter.getInstance(document, out);
            document.open(); // Otwarcie dokumentu w celu edycji i dodawania treści

            // Konfiguracja czcionek z obsługą polskich znaków diakrytycznych (kodowanie
            // Cp1250)
            // Używamy standardowej czcionki Helvetica w różnych rozmiarach i stylach
            // (gruba, normalna, kursywa)
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1250", true, 16);
            Font sectionHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1250", true, 12);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, "Cp1250", true, 11);
            Font italicFont = FontFactory.getFont(FontFactory.HELVETICA, "Cp1250", true, 10, Font.ITALIC);
            Font boldBodyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1250", true, 11);

            // 1. Nagłówek dokumentu (Tytuł zgody) - wyśrodkowany
            Paragraph title = new Paragraph(
                    "OSWIADCZENIE I ZGODA RODZICA / OPIEKUNA PRAWNEGO\nNA UDZIAL DZIECKA W WYCIECZCE SZKOLNEJ",
                    titleFont);
            title.setAlignment(Element.ALIGN_CENTER); // Wyśrodkowanie
            title.setSpacingAfter(25); // Odstęp pod tytułem
            document.add(title); // Dodanie elementu do dokumentu

            // 2. Sekcja danych ucznia
            Paragraph studentSectionHeader = new Paragraph("1. DANE UCZNIA", sectionHeaderFont);
            studentSectionHeader.setSpacingAfter(8);
            document.add(studentSectionHeader);

            // Szczegółowe dane ucznia z formatowaniem pogrubionym
            Paragraph studentDetails = new Paragraph();
            studentDetails.setFont(bodyFont);
            studentDetails.add("Imie i nazwisko ucznia: ");
            studentDetails.add(new Chunk(uczen.getImie() + " " + uczen.getNazwisko() + "\n", boldBodyFont));
            studentDetails.add("Klasa: ");
            studentDetails.add(new Chunk(
                    uczen.getKlasa() != null ? uczen.getKlasa().getNazwa() + " (" + uczen.getKlasa().getProfil() + ")"
                            : "Nieprzypisana",
                    boldBodyFont));
            studentDetails.setSpacingAfter(20);
            document.add(studentDetails);

            // 3. Sekcja danych wycieczki
            Paragraph tripSectionHeader = new Paragraph("2. INFORMACJE O WYCIECZCE", sectionHeaderFont);
            tripSectionHeader.setSpacingAfter(8);
            document.add(tripSectionHeader);

            // Szczegóły wycieczki
            Paragraph tripDetails = new Paragraph();
            tripDetails.setFont(bodyFont);
            tripDetails.add("Nazwa wycieczki: ");
            tripDetails.add(new Chunk(wycieczka.getNazwa() + "\n", boldBodyFont));
            tripDetails.add("Miejsce docelowe: ");
            tripDetails.add(new Chunk(wycieczka.getMiejsce_docelowe() + "\n", boldBodyFont));
            tripDetails.add("Termin wycieczki: ");
            tripDetails.add(
                    new Chunk("od " + wycieczka.getData_rozpoczecia() + " do " + wycieczka.getData_zakonczenia() + "\n",
                            boldBodyFont));
            tripDetails.add("Koszt na osobe: ");
            BigDecimal koszt = wycieczka.getKoszt_na_osobe() != null ? wycieczka.getKoszt_na_osobe() : BigDecimal.ZERO;
            tripDetails.add(new Chunk(koszt + " PLN\n", boldBodyFont));
            tripDetails.setSpacingAfter(20);
            document.add(tripDetails);

            // 4. Sekcja oświadczenia rodzica (Treść zgody)
            Paragraph declarationHeader = new Paragraph("3. OSWIADCZENIE I ZGODA", sectionHeaderFont);
            declarationHeader.setSpacingAfter(8);
            document.add(declarationHeader);

            Paragraph declarationText = new Paragraph();
            declarationText.setFont(bodyFont);
            declarationText.add("Wyrazam zgode na udzial mojego dziecka w wyzej wymienionej wycieczce szkolnej.\n");
            declarationText.add("Zobowiazuje sie do uregulowania kosztu wycieczki w wysokosci " + koszt
                    + " PLN w wyznaczonym terminie.\n");
            declarationText.add(
                    "Oswiadczam, ze dziecko nie ma zadnych przeciwwskazan zdrowotnych do udzialu w tej wycieczce. ");
            if (uczestnictwo.getUwagi() != null && !uczestnictwo.getUwagi().isBlank()) {
                declarationText.add("\nUwagi dotyczace zdrowia/inne: " + uczestnictwo.getUwagi() + "\n");
            } else {
                declarationText.add("\nBrak dodatkowych uwag zdrowotnych.\n");
            }
            declarationText.setSpacingAfter(40);
            document.add(declarationText);

            // 5. Linia podpisu rodzica i data
            Table signatureTable = new Table(2); // Tabela z dwoma kolumnami (data po lewej, podpis po prawej)
            signatureTable.setBorder(Table.NO_BORDER); // Brak widocznego obramowania tabeli
            signatureTable.setWidth(100); // 100% szerokości strony

            // Lewa kolumna: data podpisania
            Cell dateCell = new Cell(new Paragraph(
                    "Miejscowosc: .........................\n\nData: .........................", bodyFont));
            dateCell.setBorder(Cell.NO_BORDER);
            signatureTable.addCell(dateCell);

            // Prawa kolumna: miejsce na podpis rodzica
            Cell signatureCell = new Cell(new Paragraph(
                    ".......................................................\n(Czytelny podpis rodzica/opiekuna)",
                    bodyFont));
            signatureCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
            signatureCell.setBorder(Cell.NO_BORDER);
            signatureTable.addCell(signatureCell);

            document.add(signatureTable); // Dodanie tabeli podpisu do dokumentu

            // 6. Stopka dokumentu (informacje o systemie)
            Paragraph footer = new Paragraph("\n\n\nDokument wygenerowany automatycznie przez system WycieczeX.",
                    italicFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close(); // Zamknięcie dokumentu (ostateczny zapis bajtów)

        } catch (DocumentException e) {
            // W razie błędu tworzenia dokumentu rzucany jest wyjątek runtime
            throw new RuntimeException("Blad podczas generowania pliku PDF", e);
        }

        return out.toByteArray(); // Zwrócenie wygenerowanej tablicy bajtów dokumentu PDF
    }

    // Metoda generująca dokument PDF z listą uczestników i opiekunów wycieczki
    public byte[] generateTripParticipantsPdf(WycieczkaModel wycieczka) {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Konfiguracja czcionek z obsługą polskich znaków diakrytycznych (Cp1250)
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1250", true, 16);
            Font sectionHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1250", true, 12);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, "Cp1250", true, 10);
            Font boldBodyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1250", true, 10);
            Font italicFont = FontFactory.getFont(FontFactory.HELVETICA, "Cp1250", true, 9, Font.ITALIC);

            // Tytuł dokumentu
            Paragraph title = new Paragraph("KARTA WYCIECZKI I LISTA UCZESTNIKOW", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 1. Szczegóły wycieczki
            Paragraph detailsHeader = new Paragraph("1. INFORMACJE O WYCIECZCE", sectionHeaderFont);
            detailsHeader.setSpacingAfter(8);
            document.add(detailsHeader);

            BigDecimal koszt = wycieczka.getKoszt_na_osobe() != null ? wycieczka.getKoszt_na_osobe() : BigDecimal.ZERO;
            BigDecimal zaliczka = koszt.multiply(new BigDecimal("0.20"));

            Paragraph tripDetails = new Paragraph();
            tripDetails.setFont(bodyFont);
            tripDetails.add("Nazwa wycieczki: ");
            tripDetails.add(new Chunk(wycieczka.getNazwa() + "\n", boldBodyFont));
            tripDetails.add("Miejsce docelowe: ");
            tripDetails.add(new Chunk(wycieczka.getMiejsce_docelowe() + "\n", boldBodyFont));
            tripDetails.add("Termin wycieczki: ");
            tripDetails.add(
                    new Chunk("od " + wycieczka.getData_rozpoczecia() + " do " + wycieczka.getData_zakonczenia() + "\n",
                            boldBodyFont));
            tripDetails.add("Koszt na osobe: ");
            tripDetails.add(new Chunk(koszt + " PLN\n", boldBodyFont));
            tripDetails.add("Wymagana zaliczka (20%): ");
            tripDetails.add(new Chunk(zaliczka.setScale(2, java.math.RoundingMode.HALF_UP) + " PLN\n", boldBodyFont));
            tripDetails.setSpacingAfter(15);
            document.add(tripDetails);

            // 2. Sekcja opiekunów
            Paragraph guidesHeader = new Paragraph("2. OPIEKUNOWIE WYCIECZKI", sectionHeaderFont);
            guidesHeader.setSpacingAfter(8);
            document.add(guidesHeader);

            var opiekunowieList = wycieczka.getOpiekunowie();
            if (opiekunowieList == null || opiekunowieList.isEmpty()) {
                Paragraph noGuides = new Paragraph("Brak przypisanych opiekunów.", bodyFont);
                noGuides.setSpacingAfter(15);
                document.add(noGuides);
            } else {
                Table guidesTable = new Table(3);
                guidesTable.setWidth(100);

                Cell h1 = new Cell(new Paragraph("Imie i Nazwisko", boldBodyFont));
                Cell h2 = new Cell(new Paragraph("Rola", boldBodyFont));
                Cell h3 = new Cell(new Paragraph("Telefon kontaktowy", boldBodyFont));
                guidesTable.addCell(h1);
                guidesTable.addCell(h2);
                guidesTable.addCell(h3);

                for (var o : opiekunowieList) {
                    var nauczyciel = o.getNauczyciel();
                    String name = nauczyciel != null ? nauczyciel.getImie() + " " + nauczyciel.getNazwisko()
                            : "Nieznany";
                    String roleStr = o.getRola() != null ? o.getRola().toString() : "OPIEKUN";
                    String tel = nauczyciel != null ? nauczyciel.getTelefon_kontaktowy() : "-";

                    guidesTable.addCell(new Cell(new Paragraph(name, bodyFont)));
                    guidesTable.addCell(new Cell(new Paragraph(roleStr, bodyFont)));
                    guidesTable.addCell(new Cell(new Paragraph(tel, bodyFont)));
                }
                document.add(guidesTable);
            }

            // 3. Sekcja uczniów
            Paragraph studentsHeader = new Paragraph("3. LISTA UCZESTNIKOW (UCZNIOWIE)", sectionHeaderFont);
            studentsHeader.setSpacingAfter(8);
            document.add(studentsHeader);

            var uczestnicyList = wycieczka.getUczestniczenie();
            if (uczestnicyList == null || uczestnicyList.isEmpty()) {
                Paragraph noStudents = new Paragraph("Brak zapisanych uczestników.", bodyFont);
                noStudents.setSpacingAfter(15);
                document.add(noStudents);
            } else {
                Table studentsTable = new Table(5);
                studentsTable.setWidth(100);

                studentsTable.addCell(new Cell(new Paragraph("Lp.", boldBodyFont)));
                studentsTable.addCell(new Cell(new Paragraph("Imie i Nazwisko", boldBodyFont)));
                studentsTable.addCell(new Cell(new Paragraph("Klasa", boldBodyFont)));
                studentsTable.addCell(new Cell(new Paragraph("Status", boldBodyFont)));
                studentsTable.addCell(new Cell(new Paragraph("Uwagi / Diety", boldBodyFont)));

                int lp = 1;
                for (var u : uczestnicyList) {
                    UczenModel uczen = u.getUczen();
                    String name = uczen != null ? uczen.getImie() + " " + uczen.getNazwisko() : "Nieznany";
                    String classStr = (uczen != null && uczen.getKlasa() != null)
                            ? uczen.getKlasa().getNazwa() + " (" + uczen.getKlasa().getProfil() + ")"
                            : "Brak";
                    String statusStr = u.isCzy_jedzie() ? "Jedzie" : "Nie jedzie";
                    String notesStr = (u.getUwagi() != null && !u.getUwagi().isBlank()) ? u.getUwagi() : "-";

                    studentsTable.addCell(new Cell(new Paragraph(String.valueOf(lp++), bodyFont)));
                    studentsTable.addCell(new Cell(new Paragraph(name, bodyFont)));
                    studentsTable.addCell(new Cell(new Paragraph(classStr, bodyFont)));
                    studentsTable.addCell(new Cell(new Paragraph(statusStr, bodyFont)));
                    studentsTable.addCell(new Cell(new Paragraph(notesStr, bodyFont)));
                }
                document.add(studentsTable);
            }

            // Stopka
            Paragraph footer = new Paragraph("\nDokument wygenerowany automatycznie przez system School-Trips.",
                    italicFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Blad podczas generowania pliku PDF", e);
        }

        return out.toByteArray();
    }
}
