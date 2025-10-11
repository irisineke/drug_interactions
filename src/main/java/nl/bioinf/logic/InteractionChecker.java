package nl.bioinf.logic;

import nl.bioinf.methods.Drug;
import nl.bioinf.methods.Interaction;

import java.util.List;

public class InteractionChecker {

    public void test(List<Interaction> interactions,
                    List<Drug> drugs,
                    String firstDrugInput,
                    String secondDrugInput) {

        System.out.println("GEGEVENS BINNENGEHAALD VIA LEESBESTANDEN");
        System.out.println("Drug 1 input: " + firstDrugInput);
        System.out.println("Drug 2 input: " + secondDrugInput);
        System.out.println();

        System.out.println("=== Eerste 5 regels uit interactions.tsv ===");
        interactions.stream()
                .limit(5)
                .forEach(i -> System.out.println(
                        i.geneClaimName() + "\t" +
                                i.interactionType() + "\t" +
                                i.interactionScore() + "\t" +
                                i.drugConceptId()
                ));

        System.out.println();
        System.out.println("=== Eerste 5 regels uit drugs.tsv ===");
        drugs.stream()
                .limit(5)
                .forEach(d -> System.out.println(
                        d.drugClaimName() + "\t" +
                                d.conceptId()
                ));
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

