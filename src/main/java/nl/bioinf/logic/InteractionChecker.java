package nl.bioinf.logic;

import nl.bioinf.io.OutputGenerator;
import nl.bioinf.methods.Combination;
import nl.bioinf.methods.Drug;
import nl.bioinf.methods.Interaction;

import java.util.Set;
import java.util.stream.*;

import java.util.List;

public class InteractionChecker {

    private static String getConceptID(List<Drug> drugs, String drugInput){
        return drugs.stream()
                .filter(drug -> drug.drugClaimName().equalsIgnoreCase(drugInput)) // gebruikt getter
                .map(Drug::conceptId) // pakt concept id
                .findFirst() // pakt eerste (en als t goed is enige) match
                .orElseThrow(()-> new IllegalArgumentException("Drug not found: " + drugInput));
    }

    public Set<String> geneOverlap(List<Interaction> interactions,
                                   List<Drug> drugs,
                                   String firstDrugInput,
                                   String secondDrugInput) {

        String idDrug1 = getConceptID(drugs, firstDrugInput);
        String idDrug2 = getConceptID(drugs, secondDrugInput);


        System.out.println("==== Find overlap genes ==== ");
        System.out.println("Drug 1 input: " + firstDrugInput);
        System.out.println("Drug 2 input: " + secondDrugInput);
        System.out.println(" ");


// genen ophalen die drug beïnvloeden
        Set<String> genesDrug1 = interactions.stream()
                .filter(interaction -> interaction.drugConceptId().equals(idDrug1))
                .map(Interaction :: geneClaimName)
                .collect(Collectors.toSet()); // bewaart in set

        Set<String> genesDrug2 = interactions.stream()
                .filter(interaction -> interaction.drugConceptId().equals(idDrug2))
                .map(Interaction :: geneClaimName)
                .collect(Collectors.toSet()); // bewaart in set

// Overlap tussen genen vinden
        Set<String> overlap = genesDrug1.stream() // eerste alle genen van drug1 pakken
            .filter(genesDrug2::contains) // bewaar alleen die die ook in drug 2 voorkomen
            .collect(Collectors.toSet());

        // ! door translate gegooid, later ms correct engels maken
        System.out.println("Number of genes influenced by " + firstDrugInput + ": " + genesDrug1.size());
        System.out.println("Number of genes influenced by " + secondDrugInput + ": " + genesDrug2.size());


        // printen overlap: wat als er geen overlap is, en else print alle overlap genen:
        if (overlap.isEmpty()){
            System.out.println("No overlap found.");}
        else {
            System.out.println("Number of overlapping genes: " + overlap.size());
            System.out.println("Overlapping genes: ");
            overlap.forEach(System.out::println); // print alle regels uit overlap
        }

        return overlap; // overlap teruggeven aan ArgumentParser
    }




    public static void GetInteraction(String[] args) {
        System.out.println("-");
    }

    public static void CompareInteractionScore(String[] args) {
        System.out.println("-");
    }

    public static String[] getInteractionTypes(List<Interaction> interactions,
                                               List<Drug> drugs,
                                               String firstDrugInput,
                                               String secondDrugInput) {

        String idDrug1 = getConceptID(drugs, firstDrugInput);
        String idDrug2 = getConceptID(drugs, secondDrugInput);

        // interaction_type ophalen van drug (niet volgorde influenced)
        String typeDrug1 = interactions.stream()
                .filter(interaction -> interaction.drugConceptId().equals(idDrug1))
                .map(Interaction::interactionType)
                .findFirst()
                .orElse("Unkown");

        String typeDrug2 = interactions.stream()
                .filter(interaction -> interaction.drugConceptId().equals(idDrug2))
                .map(Interaction::interactionType)
                .findFirst()
                .orElse("Unkown");


        return new String[]{typeDrug1, typeDrug2};
    }


    public static String getCombinationResult(List<Interaction> interactions,
                                              List<Drug> drugs,
                                              String firstDrugInput,
                                              String secondDrugInput,
                                              List<Combination> combinations,
                                              Set<String> overlap) {

        if (overlap.isEmpty()) {
            System.out.println("No gene overlap found; skipping combination result.");
            return "not applicable";
        }

        String[] types = getInteractionTypes(interactions, drugs, firstDrugInput, secondDrugInput);
        String typeDrug1 = types[0];
        String typeDrug2 = types[1];

        System.out.println("==== Find type drugs ==== ");
        System.out.println(firstDrugInput + " type: " + typeDrug1);
        System.out.println(secondDrugInput + " type: " + typeDrug2);
        System.out.println();

        // stay with me:
        // zoekt naar de types in de drug_combination.tsv en geeft resultaat (kolom met combinatie resultaat) terug
        for (Combination comb : combinations) {
            boolean match = comb.drugType1().equalsIgnoreCase(typeDrug1) && comb.drugType2().equalsIgnoreCase(typeDrug2) ||
                    comb.drugType1().equalsIgnoreCase(typeDrug2) && comb.drugType2().equalsIgnoreCase(typeDrug1);

            if (match) {
                System.out.println("==== Combination drugs Result ==== ");
                System.out.println("Combination result: " + comb.resultaat());
                System.out.println();
                return comb.resultaat();
            }
        }

        System.out.println("==== Combination drugs Result ==== ");
        System.out.println("Combination result is unknown");
        System.out.println();
        return "Unknown";
        }
}



