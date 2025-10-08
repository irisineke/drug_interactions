package nl.bioinf;

import java.util.HashSet;
import java.io.*;
import java.util.*;


public class IntTypeSet {
    // checken welke types er in de interaction kolom staan
    public static void main(String[] args) throws Exception {
        // had ff geen zin in gedoe, later dynamisch maken
        String path = "/Users/irisineke/IdeaProjects/drug_interactions/data/raw/interactions.tsv";
        int indexKolom = 5; // interaction_type kolom

        Set<String> uniekeWaardes = new HashSet<>();

        // inlezen bestand
        try (BufferedReader bReader = new BufferedReader(new FileReader(path))) {
            String regel;
            while ((regel = bReader.readLine()) != null) {
                String [] kolommen = regel.split("\t"); // tsv is blijkbaar Tab Separated Values
                if (kolommen.length > indexKolom) { // kolom aanwezigheid checken
                    uniekeWaardes.add(kolommen[indexKolom]);
                }
            }
        }

        for (String waarde : uniekeWaardes) {
            System.out.println(waarde);
        }
    }

}