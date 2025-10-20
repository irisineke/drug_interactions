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
                                   String secondDrugInput,
                                   StringBuilder outputSB) {

        String idDrug1 = getConceptID(drugs, firstDrugInput);
        String idDrug2 = getConceptID(drugs, secondDrugInput);


        outputSB.append("==== Find overlap genes ==== \n");
        outputSB.append("Drug 1 input: ").append(firstDrugInput).append("\n");
        outputSB.append("Drug 2 input: ").append(secondDrugInput).append("\n\n");



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
        outputSB.append("Number of genes influenced by ").append(firstDrugInput).append( ": ").append(genesDrug1.size()).append("\n");
        outputSB.append("Number of genes influenced by ").append(secondDrugInput).append(": ").append(genesDrug2.size()).append("\n\n");


        // printen overlap: wat als er geen overlap is, en else print alle overlap genen:
        if (overlap.isEmpty()){
            outputSB.append("No overlap found.");}
        else {
            outputSB.append("Number of overlapping genes: ").append(overlap.size()).append("\n");
            outputSB.append("Overlapping genes: ").append("\n");
            overlap.forEach(gene ->outputSB.append(gene).append("\n")); // print alle regels uit overlap
        }

        outputSB.append("\n");
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
                                              Set<String> overlap,
                                              StringBuilder outputSB) {

        if (overlap.isEmpty()) {
            outputSB.append("No gene overlap found; skipping combination result.").append("\n");
            return "unknown";
        }

        String[] types = getInteractionTypes(interactions, drugs, firstDrugInput, secondDrugInput);
        String typeDrug1 = types[0];
        String typeDrug2 = types[1];

        outputSB.append("==== Find type drugs ==== ").append("\n");
        outputSB.append(firstDrugInput).append(" type: ").append(typeDrug1).append("\n");
        outputSB.append(secondDrugInput).append(" type: ").append(typeDrug2).append("\n\n");


        // stay with me:
        // zoekt naar de types in de drug_combination.tsv en geeft resultaat (kolom met combinatie resultaat) terug
        for (Combination comb : combinations) {
            boolean match = comb.drugType1().equalsIgnoreCase(typeDrug1) && comb.drugType2().equalsIgnoreCase(typeDrug2) ||
                    comb.drugType1().equalsIgnoreCase(typeDrug2) && comb.drugType2().equalsIgnoreCase(typeDrug1);

            if (match) {
                outputSB.append("==== Combination drugs Result ==== ").append("\n");
                outputSB.append("Combination result: ").append(comb.resultaat()).append("\n\n");
                return comb.resultaat();
            }
        }

        outputSB.append("==== Combination drugs Result ==== ").append("\n");
        outputSB.append("Combination result is unknown").append("\n\n");
        return "Unknown";
        }
}



