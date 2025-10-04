package nl.bioinf;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class LeesBestanden {
    private final Path rawInteractions;
    private final Path rawDrugs;
    private final Path preparedInteractions;
    private final Path preparedDrugs;

    public LeesBestanden(Path rawInteractions, Path rawDrugs,
                         Path preparedInteractions, Path preparedDrugs) {
        this.rawInteractions = rawInteractions;
        this.rawDrugs = rawDrugs;
        this.preparedInteractions = preparedInteractions;
        this.preparedDrugs = preparedDrugs;
    }

    /**
     * Zorgt dat de prepared-bestanden bestaan:
     * - Als prepared al bestaat → gebruik die.
     * - Anders, als raw bestaat → maak nieuwe prepared-bestanden met alleen de gevraagde kolommen.
     * - Anders → gooi een foutmelding.
     */
    public void ensurePreparedData(List<String> keepInteractions, List<String> keepDrugs) throws IOException {
        boolean preparedExists = Files.exists(preparedInteractions) && Files.exists(preparedDrugs);
        if (preparedExists) return;

        boolean rawExists = Files.exists(rawInteractions) && Files.exists(rawDrugs);
        if (rawExists) {
            Files.createDirectories(preparedInteractions.getParent());
            filterTsvKeepColumnsByHeader(rawInteractions, preparedInteractions, keepInteractions);
            filterTsvKeepColumnsByHeader(rawDrugs, preparedDrugs, keepDrugs);

            if (!(Files.exists(preparedInteractions) && Files.exists(preparedDrugs))) {
                throw new IllegalStateException("Aanmaken van prepared-bestanden is mislukt.");
            }
            return;
        }

        throw new IllegalStateException(
                "Data ontbreekt: niet gevonden\n" +
                        "prepared: " + preparedInteractions + " & " + preparedDrugs + "\n" +
                        "raw: " + rawInteractions + " & " + rawDrugs
        );
    }

    /**
     * Leest een TSV (met header) en schrijft een nieuw bestand met alleen de gewenste kolommen.
     */
    private void filterTsvKeepColumnsByHeader(Path input, Path output, List<String> headersToKeep) throws IOException {
        if (!Files.exists(input)) {
            throw new FileNotFoundException("Bestand ontbreekt: " + input.toAbsolutePath());
        }

        try (BufferedReader br = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            String headerLine = br.readLine();
            if (headerLine == null) throw new IllegalStateException("Lege tabel: " + input);

            String delimiter = "\t";
            String[] headers = headerLine.split("\t", -1);
            ;

            // Eventuele BOM verwijderen
            if (headers.length > 0) {
                headers[0] = headers[0].replace("\uFEFF", "").trim();
            }

            Map<String, Integer> indexByHeader = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                indexByHeader.put(headers[i].trim(), i);
            }

            // Check op ontbrekende kolommen
            List<String> missing = new ArrayList<>();
            for (String h : headersToKeep) {
                if (!indexByHeader.containsKey(h)) missing.add(h);
            }
            if (!missing.isEmpty()) {
                throw new IllegalStateException(
                        "Kolom(men) ontbreken in " + input + ": " + missing +
                                "\nBeschikbare kolommen: " + indexByHeader.keySet()
                );
            }

            // Schrijf nieuwe TSV met alleen de gewenste kolommen
            Files.createDirectories(output.getParent());
            try (BufferedWriter bw = Files.newBufferedWriter(output, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

                bw.write(String.join(delimiter, headersToKeep));
                bw.newLine();

                String line;
                while ((line = br.readLine()) != null) {
                    String[] cells = line.split("\t", -1);
                    List<String> out = new ArrayList<>();
                    for (String h : headersToKeep) {
                        int idx = indexByHeader.get(h);
                        out.add(idx < cells.length ? cells[idx] : "");
                    }
                    bw.write(String.join(delimiter, out));
                    bw.newLine();
                }
            }
        }
    }

    /**
     * Print de eerste n regels van een bestand.
     */
    public static void printFirstNLines(Path file, int n) throws IOException {
        if (!Files.exists(file)) throw new FileNotFoundException("Bestand ontbreekt: " + file);
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            for (int i = 0; i < n; i++) {
                String line = br.readLine();
                if (line == null) break;
                System.out.println(line);
            }
        }
    }

    /**
     * headers uit een TSV te printen.
     */
    public static void printHeaders(Path file) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String headerLine = br.readLine();
            if (headerLine != null) {
                String[] headers = headerLine.split("\t", -1);
                System.out.println("Headers in " + file + ":");
                for (String h : headers) {
                    System.out.println("- " + h.trim());
                }
            }
        }
    }
    ArgumentParser argparser = new ArgumentParser();
}




