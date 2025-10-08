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
    @Option(names = { "-intF", "--interactionsFile" },
            paramLabel = "interactionsFile", // zo heet hij in help
            description = "the input file: interactions.tsv", // ook in help
            required = true)
    File interactionsFile;

    @Option(names = { "-drF", "--drugsFile" },
            paramLabel = "drugsFile", // zo heet hij in help
            description = "the input file: drugs.tsv", // ook in help
            required = true)
    File drugsFile;

    @Option(names = {"-drug1", "-d1"},
            paramLabel = "firstDrugInput",
            description = "put the first drug you want to compare here",
            required = true)
    File firstDrugInput;

    @Option(names = {"-drug2", "-d2"},
            paramLabel = "secondDrugInput",
            description = "put the second drug you want to compare here",
            required = true)
    File secondDrugInput;


    @Option(names = {"-output", "-o"},
            paramLabel = "output",
            description = "put the spath to where you want the output to land",
            required = true)
    File output;


    @Override
    public void run() {

        try {
            // roept de fileNotEmptyCheck aan met de files als input
            fileNotEmptyCheck("Interactions file", interactionsFile.getAbsolutePath());
            fileNotEmptyCheck("Drugs file", drugsFile.getAbsolutePath());


            prepareAndPrintData();

        } catch (Exception e) {
            System.err.println("❌ FOUT: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }


    public void fileNotEmptyCheck(String name, String value){
        // verwijder deze regel niet nog een keer per ongeluk pls !!!
        File file = new File(value);

        // checken of opgegeven file bestaat
        if (!file.exists()) {
            throw new IllegalArgumentException("❌ FOUT: " + name + " does not exist :(");
        }
        // checken of opgegeven file daadwerklijk een file is (super handig) (ipv directory bv)
        if (!file.isFile()){
            throw new IllegalArgumentException("❌ FOUT: " + name + " is not a file :(");
        }
        // checken of file niet leeg is
            // ! moest dit nou met == of .equals ?
        if (file.length() == 0) {
            throw new IllegalArgumentException("❌ FOUT: " + name + " is empty :(");
        }
    }


    private void prepareAndPrintData() throws Exception {
        // de paden uit de input gebruiken als raw bestanden
        Path rawInteractions = interactionsFile.toPath();
        Path rawDrugs = drugsFile.toPath();

       // dot wordt dus nog aangepast als we in een class tijdelijk op gaan slaan !
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

//    tijdleijk voor uml, 3e en 4e paths gaan later weg als t goed is.
    LeesBestanden leesBestanden = new LeesBestanden(interactionsFile, drugsFile, interactionsFile, drugsFile);
    InteractionChecker checker = new InteractionChecker(firstDrugInput, secondDrugInput);
    OutputGenerator generator = new OutputGenerator(output);

}




// runnen: ./gradlew run --args='-intF data/raw/interactions.tsv -drF data/raw/drugs.tsv -d1 a -d2 b -o /data'