package nl.bioinf.logic;

import nl.bioinf.methods.Drug;
import nl.bioinf.methods.Interaction;

import java.util.Set;
import java.util.stream.*;

import java.util.List;

public class InteractionChecker {

    public void test(List<Interaction> interactions,
                    List<Drug> drugs,
                    String firstDrugInput,
                    String secondDrugInput) {

        System.out.println("==== Find overlap genes ==== ");
        System.out.println("Drug 1 input: " + firstDrugInput);
        System.out.println("Drug 2 input: " + secondDrugInput);
        System.out.println();

//        System.out.println("=== Eerste 5 regels uit interactions.tsv ===");
//        interactions.stream()
//                .limit(5)
//                .forEach(i -> System.out.println(
//                        i.geneClaimName() + "\t" +
//                                i.interactionType() + "\t" +
//                                i.interactionScore() + "\t" +
//                                i.drugConceptId()
//                ));

//        System.out.println();
//        System.out.println("=== Eerste 5 regels uit drugs.tsv ===");
//        drugs.stream()
//                .limit(5)
//                .forEach(d -> System.out.println(
//                        d.drugClaimName() + "\t" +
//                                d.conceptId()
//                ));

//  concept id v`n opgegeven drugnaam ophalen
        // met stream erdoorheen loopen
        String idDrug1 = drugs.stream()
                .filter(drug -> drug.drugClaimName().equalsIgnoreCase(firstDrugInput)) // gebruikt getter
                .map(Drug::conceptId) // pakt concept id
                .findFirst() // pakt eerste (en als t goed is enige) match
                .orElseThrow(()-> new IllegalArgumentException("Drug not found: " + firstDrugInput));

        String idDrug2 = drugs.stream()
                .filter(drug -> drug.drugClaimName().equalsIgnoreCase(secondDrugInput)) // gebruikt getter
                .map(Drug::conceptId) // pakt concept id
                .findFirst() // pakt eerste (en als t goed is enige) match
                .orElseThrow(()-> new IllegalArgumentException("Drug not found: " + secondDrugInput));

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

    }
    public static void GetDrugsID(String[] args) {
        System.out.println("-");
    }

    public static void GetInteraction(String[] args) {
        System.out.println("-");
    }

    public static void CompareInteractionScore(String[] args) {
        System.out.println("-");
    }

    public static void CompareInteractionTypes(String[] args) {
        System.out.println("-");
    }
}

