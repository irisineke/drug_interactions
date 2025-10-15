package nl.bioinf.logic;

import nl.bioinf.io.OutputGenerator;
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
        System.out.println();


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

    public static String CompareInteractionTypes(List<Interaction> interactions,
                                               List<Drug> drugs,
                                               String firstDrugInput,
                                               String secondDrugInput) {

//        interaction_type

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


        System.out.println("==== Find type drugs ==== ");
        System.out.println("Drug 1 type: " + typeDrug1);
        System.out.println("Drug 2 type: " + typeDrug2);
        System.out.println();

        return typeDrug1 + typeDrug2;
    }}

