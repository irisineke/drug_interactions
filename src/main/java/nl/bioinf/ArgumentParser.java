package nl.bioinf;

import nl.bioinf.io.*;
import nl.bioinf.io.OutputGenerator;
import nl.bioinf.logic.InteractionChecker;
import picocli.CommandLine.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import nl.bioinf.models.Interaction;
import nl.bioinf.models.Drug;
import nl.bioinf.models.Combination;

/**
 * The {@code ArgumentParser} class defines and handles all command-line arguments
 * for the Drug Interactions program using the Picocli library.
 * <p>
 * This program reads two input files (drugs.tsv and interactions.tsv),
 * accepts two drug names from the user, and determines whether they can be
 * safely combined. The results are written to an output file.
 * <p>
 * Note: this program provides indicative analysis only — it does not offer medical advice.
 */



@Command (name = "Drug Interactions",
        mixinStandardHelpOptions = true,
        version = "Drug Interactions 1.0",
        description = "This program uses two drug inputs and two file inputs (drug.tsv and interaction.tsv) and performs an assessment. The program then assesses whether these can be safely combined. The program does not offer binding medical advice, but rather indicative support to identify potential risks at an earlier stage.\n")


public class ArgumentParser implements Runnable {
    @Option(names = { "-intF", "--interactionsFile" },
            paramLabel = "interactionsFile",
            description = "the input file. for example: interactions.tsv", // ook in help
            required = true)
    File interactionsFile;

    @Option(names = { "-drF", "--drugsFile" },
            paramLabel = "drugsFile",
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

    /**
     * Executes the main workflow of the Drug Interactions program.
     * <p>
     * The method performs the following steps:
     * <ol>
     *     <li>Validates that both input files exist and are not empty.</li>
     *     <li>Reads and processes the input data using {@link ReadFiles}.</li>
     *     <li>Validates the output path using {@link Validate}.</li>
     *     <li>Analyzes drug interactions using {@link InteractionChecker}.</li>
     *     <li>Generates the output file using {@link OutputGenerator}.</li>
     * </ol>
     * If any step fails, an error message is printed and the program exits with status code 1.
     */
    @Override
    public void run() {

        try {
            fileNotEmptyCheck("Interactions file", interactionsFile.getAbsolutePath());
            fileNotEmptyCheck("Drugs file", drugsFile.getAbsolutePath());

            ReadFiles lb = new ReadFiles(interactionsFile, drugsFile);
            List<Interaction> interactions = lb.processInteractions();
            List<Drug> drugs = lb.processDrugs();
            List<Combination> combinations = lb.processCombinations();

            Validate.validateOutputPath(output);

            InteractionChecker checker = new InteractionChecker(interactions, drugs, combinations, firstDrugInput, secondDrugInput);
//            StringBuilder outputSB = new StringBuilder();

            Set<String> overlap = checker.geneOverlap();
            checker.getInteractionTypes();
            String combinationResult = checker.getCombinationResult(overlap);
            List<InteractionChecker.GeneScore> geneScores = checker.getInteractionScorePerGene(overlap);
            checker.compareInteractionScore(combinationResult, geneScores, overlap);

            OutputGenerator generator = new OutputGenerator(output);
            generator.generateOutput(checker.getOutputSB());


        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }


    public void fileNotEmptyCheck(String name, String value){
        File file = new File(value);

        if (!file.exists()) {
            throw new IllegalArgumentException("ERROR: " + name + " does not exist :(");
        }
        if (!file.isFile()){
            throw new IllegalArgumentException("ERROR: " + name + " is not a file :(");
        }

        if (file.length() == 0) {
            throw new IllegalArgumentException("ERROR: " + name + " is empty :(");
        }

    }
}
