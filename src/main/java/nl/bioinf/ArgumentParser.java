package nl.bioinf;

import nl.bioinf.io.*;
import nl.bioinf.io.OutputGenerator;
import nl.bioinf.logic.InteractionChecker;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.IParameterConsumer;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;


import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.Stack;

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
@Command(
        name = "Drug Interactions",
        mixinStandardHelpOptions = true,
        version = "Drug Interactions 1.0",
        description = "This program uses two drug inputs and two file inputs (drug.tsv and interaction.tsv) and performs an assessment. The program then assesses whether these can be safely combined. The program does not offer binding medical advice, but rather indicative support to identify potential risks at an earlier stage.\n"
)
public class ArgumentParser implements Runnable {

    /**
     * Consumer die na het eerste token alle opeenvolgende tokens (tot het volgende
     * argument dat met '-') samenvoegt, zodat waarden met spaties ook zonder quotes werken.
     */
    static class MultiWordParameterConsumer implements IParameterConsumer {
        @Override
        public void consumeParameters(Stack<String> args, ArgSpec argSpec, CommandSpec commandSpec) {
            if (args.isEmpty()) {
                argSpec.setValue(null);
                return;
            }
            StringBuilder sb = new StringBuilder();
            while (!args.isEmpty()) {
                String next = args.peek();
                // Stop bij volgende optie
                if (next.startsWith("-")) break;
                sb.append(args.pop());
                if (!args.isEmpty() && !args.peek().startsWith("-")) {
                    sb.append(" ");
                }
            }
            argSpec.setValue(sb.toString());
        }
    }

    @Option(names = { "-intF", "--interactionsFile" },
            paramLabel = "interactionsFile",
            description = "the input file. for example: interactions.tsv",
            required = true)
    File interactionsFile;

    @Option(names = { "-drF", "--drugsFile" },
            paramLabel = "drugsFile",
            description = "the input file. for example: drugs.tsv",
            required = true)
    File drugsFile;

    @Option(names = {"--drug1", "-d1"},
            paramLabel = "firstDrugInput",
            description = "put the first drug you want to compare here",
            required = true,
            parameterConsumer = MultiWordParameterConsumer.class)
    String firstDrugInput;

    @Option(names = {"--drug2", "-d2"},
            paramLabel = "secondDrugInput",
            description = "put the second drug you want to compare here",
            required = true,
            parameterConsumer = MultiWordParameterConsumer.class)
    String secondDrugInput;

    @Option(names = {"--output", "-o"},
            paramLabel = "output",
            description = "put the path to where you want the output to land",
            required = true)
    Path output;

    @Override
    public void run() {
        try {
            Validate.validateDifferentDrugs(firstDrugInput, secondDrugInput);
            Validate.validateOutputPath(output);

            ReadFiles lb = new ReadFiles(interactionsFile, drugsFile);
            List<Interaction> interactions = lb.processInteractions();
            List<Drug> drugs = lb.processDrugs();
            List<Combination> combinations = lb.processCombinations();


            InteractionChecker checker = new InteractionChecker(
                    interactions, drugs, combinations, firstDrugInput, secondDrugInput);

            Set<String> overlap = checker.geneOverlap();
            checker.getInteractionTypes();
            String combinationResult = checker.getCombinationResult(overlap);
            List<InteractionChecker.GeneScore> geneScores = checker.getInteractionScorePerGene(overlap);
            checker.compareInteractionScore(combinationResult, geneScores, overlap);

            OutputGenerator generator = new OutputGenerator(output);
            generator.generateOutput(checker.getOutputSB());

        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }
}