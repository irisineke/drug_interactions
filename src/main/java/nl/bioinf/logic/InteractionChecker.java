package nl.bioinf.logic;

import nl.bioinf.io.OutputGenerator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

// dummy class voor uml
public class InteractionChecker {
    public void run(List<String> interactions, List<String> drugs) {
        System.out.println("=== Interactions-bestand ===");
        interactions.stream().limit(5).forEach(System.out::println);;

        System.out.println("\n=== Drugs-bestand ===");
        drugs.stream().limit(100).forEach(System.out::println);;
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

    Path output = Paths.get("/data");
    OutputGenerator generator = new OutputGenerator(output);
}
