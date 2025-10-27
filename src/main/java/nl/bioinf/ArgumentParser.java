package nl.bioinf;

import nl.bioinf.io.*;
import nl.bioinf.io.OutputGenerator;
import nl.bioinf.logic.InteractionChecker;
import picocli.CommandLine.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import nl.bioinf.methods.Interaction;
import nl.bioinf.methods.Drug;
import nl.bioinf.methods.Combination;



// name, help en version erin, description
@Command (name = "Drug Interactions",
        mixinStandardHelpOptions = true,
        version = "Drug Interactions 1.0",
        description = "This program uses two drug inputs and two file inputs (drug.tsv and interaction.tsv) and performs an assessment. The program then assesses whether these can be safely combined. The program does not offer binding medical advice, but rather indicative support to identify potential risks at an earlier stage.\n")


// Callable: Integer of wat anders teruggeven ?
public class ArgumentParser implements Runnable {
//    vangt input file op
    @Option(names = { "-intF", "--interactionsFile" },
            paramLabel = "interactionsFile", // zo heet hij in help
            description = "the input file. for example: interactions.tsv", // ook in help
            required = true)
    File interactionsFile;

    @Option(names = { "-drF", "--drugsFile" },
            paramLabel = "drugsFile", // zo heet hij in help
            description = "the input file. for example: drugs.tsv", // ook in help
            required = true)
    File drugsFile;

    @Option(names = {"--drug1", "-d1"},
            paramLabel = "firstDrugInput",
            description = "put the first drug you want to compare here",
            required = true)
    String firstDrugInput;

    @Option(names = {"--drug2", "-d2"},
            paramLabel = "secondDrugInput",
            description = "put the second drug you want to compare here",
            required = true)
    String secondDrugInput;

    @Option(names = {"--output", "-o"},
            paramLabel = "output",
            description = "put the path to where you want the output to land",
            required = true)
    Path output;


    @Override
    public void run() {

        try {
            // roept de fileNotEmptyCheck aan met de files als input
            fileNotEmptyCheck("Interactions file", interactionsFile.getAbsolutePath());
            fileNotEmptyCheck("Drugs file", drugsFile.getAbsolutePath());

            ReadFiles lb = new ReadFiles(interactionsFile, drugsFile);
            List<Interaction> interactions = lb.processInteractions();
            List<Drug> drugs = lb.processDrugs();
            List<Combination> combinations = lb.processCombinations();

            Validate.validateOutputPath(output);

            InteractionChecker checker = new InteractionChecker();
            StringBuilder outputSB = new StringBuilder();
            Set<String> overlap = checker.geneOverlap(interactions, drugs, firstDrugInput, secondDrugInput, outputSB);
            String[] type = checker.getInteractionTypes(interactions, drugs, firstDrugInput, secondDrugInput);
            String combinationResult = checker.getCombinationResult(interactions, drugs, firstDrugInput, secondDrugInput, combinations, overlap, outputSB);
            List<String> GetInteractionScore = checker.GetInteractionScorePerGene(interactions, drugs, firstDrugInput, secondDrugInput, overlap, outputSB);
            String getinteractionScore = checker.CompareInteractionScore(interactions, drugs, firstDrugInput, secondDrugInput, combinations, overlap, outputSB);

            OutputGenerator generator = new OutputGenerator(output);
            generator.generateOutput(outputSB);


        } catch (Exception e) {
            System.err.println(e.getMessage());
//            e.printStackTrace(); // alleen gebruiken bij testen
            System.exit(1);
        }
    }


    public void fileNotEmptyCheck(String name, String value){
        // verwijder deze regel niet nog een keer per ongeluk pls !!!
        File file = new File(value);

        // checken of opgegeven file bestaat
        if (!file.exists()) {
            throw new IllegalArgumentException("❌ ERROR: " + name + " does not exist :(");
        }
        // checken of opgegeven file daadwerklijk een file is (super handig) (ipv directory bv)
        if (!file.isFile()){
            throw new IllegalArgumentException("❌ ERROR: " + name + " is not a file :(");
        }
        // checken of file niet leeg is
            // ! moest dit nou met == of .equals ?
        if (file.length() == 0) {
            throw new IllegalArgumentException("❌ ERROR: " + name + " is empty :(");
        }

    }
}




// runnen zonder shadow jar:
// ./gradlew run --args='-intF data/raw/interactions.tsv -drF data/raw/drugs.tsv -d1 a -d2 b -o /data'
// ./gradlew run --args='-intF /home/Jonkerjas/Downloads/interactions.tsv  -drF /home/Jonkerjas/Downloads/drugs.tsv -d1 a -d2 b -o /data'


// met shadowjar: eerst op schadowjar klikken (in Gradle, rechts -->)
// java -jar build/libs/drug_interactions-1.0-SNAPSHOT-all.jar -intF data/raw/interactions.tsv -drF data/raw/drugs.tsv -d1 a -d2 b -o /data
// java -jar build/libs/drug_interactions-1.0-SNAPSHOT-all.jar -intF /home/Jonkerjas/Downloads/interactions.tsv  -drF /home/Jonkerjas/Downloads/drugs.tsv -d1 a -d2 b -o /data


//Met test genen er ook bij:
// voor checken overlap:
// ./gradlew run --args='-intF data/raw/interactions.tsv -drF data/raw/drugs.tsv -d1 clonidine -d2 Compro -o /Users/irisineke/Downloads/test_overlap.txt'
// voor checken geen overlap:
// ./gradlew run --args='-intF data/raw/interactions.tsv -drF data/raw/drugs.tsv -d1 Savella -d2 Acthar -o /Users/irisineke/Downloads/test_overlap.txt'

// voor checken combinaties:
// ./gradlew run --args='-intF data/raw/interactions.tsv -drF data/raw/drugs.tsv -d1 clonidine -d2 dicyclomine -o /Users/irisineke/Downloads/test_overlap.txt'
// voor checken geen resultaat (door null of unknown):
// ./gradlew run --args='-intF data/raw/interactions.tsv -drF data/raw/drugs.tsv -d1 clonidine -d2 Compro -o /Users/irisineke/Downloads/test_overlap.txt'

