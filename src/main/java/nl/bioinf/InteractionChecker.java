package nl.bioinf;

import java.util.List;

// dummy class voor uml
public class InteractionChecker {
    public void run(List<String> interactions, List<String> drugs) {
        System.out.println("=== Interactions-bestand ===");
        interactions.stream().limit(5).forEach(System.out::println);;

        System.out.println("\n=== Drugs-bestand ===");
        drugs.stream().limit(100).forEach(System.out::println);;

    }
}
