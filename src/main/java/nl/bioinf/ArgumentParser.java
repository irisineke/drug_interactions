package nl.bioinf;

import picocli.CommandLine.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;



// name, help en version erin, description
@Command (name = "argumentparser",
        mixinStandardHelpOptions = true,
        version = "argumentparser 1.0",
        description = "handels the argument input")


// Callable: Integer of wat anders teruggeven ?
public class ArgumentParser implements Runnable {
//    vangt input file op
    @Option(names = { "-f", "--file" },
            paramLabel = "inputFile", // zo heet hij in help
            description = "the input file", // ook in help
            required = false)
    File inputFile;


    @Override
    public void run() {
        try {
            prepareAndPrintData();
        } catch (Exception e) {
            System.err.println("❌ FOUT: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void prepareAndPrintData() throws Exception {
//      checkt of de inputFile uit de commandline is meegegeven
        Path rawInteractions = inputFile != null ? inputFile.toPath()
                : Path.of("data/raw/interactions.tsv");
        Path rawDrugs = Path.of("data/raw/drugs.tsv");

        Path preparedInteractions = Path.of("data/prepared/interactions.tsv");
        Path preparedDrugs = Path.of("data/prepared/drugs.tsv");

        List<String> keepInteractions = List.of(
                "gene_claim_name",
                "interaction_type",
                "interaction_score",
                "drug_concept_id"
        );

        List<String> keepDrugs = List.of(
                "drug_claim_name",
                "concept_id"
        );

        LeesBestanden preparer = new LeesBestanden(
                rawInteractions, rawDrugs, preparedInteractions, preparedDrugs
        );

        preparer.ensurePreparedData(keepInteractions, keepDrugs);

        System.out.println("✅ Prepared bestanden OK:");
        System.out.println(" - " + preparedInteractions.toAbsolutePath());
        System.out.println(" - " + preparedDrugs.toAbsolutePath());

        System.out.println("\nVoorbeeld (eerste 2 regels interactions.tsv):");
        LeesBestanden.printFirstNLines(preparedInteractions, 2);

        System.out.println("\nVoorbeeld (eerste 2 regels drugs.tsv):");
        LeesBestanden.printFirstNLines(preparedDrugs, 2);
    }

//    public static void main(String[] args) {
//        int exitCode = new CommandLine(new ArgumentParser()).execute(args);
//        System.exit(exitCode);
//    }
}
// maakt commandline object van de class argumentparser
//    public static void main(String[] args) {
//        int exitCode = new picocli.CommandLine(new ArgumentParser()).execute(args);
//        System.exit(exitCode); // geef mee aan het systeem of het goed gaat (0 of 1)
//          System.exit(new CommandLine(new MyApp()).execute(args));
//    }



// runnen: ./gradlew run --args="--file data/raw/interactions.tsv"