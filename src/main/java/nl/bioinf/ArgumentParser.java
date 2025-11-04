package nl.bioinf;

import nl.bioinf.io.*;
import nl.bioinf.io.OutputGenerator;
import nl.bioinf.logic.InteractionChecker;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.IParameterConsumer;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine;

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
 */
@Command(
        name = "Drug Interactions",
        mixinStandardHelpOptions = true,
        version = "Drug Interactions 1.0",
        description = "This program uses two drug inputs and two file inputs (drug.tsv and interaction.tsv) and performs an assessment. The program then assesses whether these can be safely combined. The program does not offer binding medical advice, but rather indicative support to identify potential risks at an earlier stage.\n"
)
public class ArgumentParser implements Runnable {

    /**
     * A custom {@link IParameterConsumer} implementation that allows command-line arguments
     * consisting of multiple words (e.g., drug names with spaces) to be treated as a single parameter value.
     * <p>
     * Normally, Picocli splits arguments by whitespace, so an input like
     * {@code --drug1 "Acetyl salicylic acid"} would need quotes. This consumer instead
     * combines consecutive tokens into a single string until another option (starting with "-") is encountered.
     * </p>
     *
     * <p><strong>Example:</strong><br>
     * Input: {@code --drug1 Acetyl salicylic acid --drug2 Ibuprofen}<br>
     * Result: {@code firstDrugInput = "Acetyl salicylic acid"} and {@code secondDrugInput = "Ibuprofen"}
     * </p>
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
                if (next.startsWith("-")) break;
                sb.append(args.pop());
                if (!args.isEmpty() && !args.peek().startsWith("-")) {
                    sb.append(" ");
                }
            }
            argSpec.setValue(sb.toString());
        }
    }

    @Option(names = {"-intF", "--interactionsFile"}, required = true,
            paramLabel = "FILE",
            description = "Path to interaction TSV file (e.g., interaction.tsv).")
    File interactionsFile;

    @Option(names = {"-drF", "--drugsFile"}, required = true,
            paramLabel = "FILE",
            description = "Path to drug TSV file (e.g., drug.tsv).")
    File drugsFile;

    @Option(names = {"--drug1", "-d1"}, required = true,
            parameterConsumer = MultiWordParameterConsumer.class,
            paramLabel = "\"NAME\"",
            description = "First drug name (supports multi-word values).")
    String firstDrugInput;

    @Option(names = {"--drug2", "-d2"}, required = true,
            parameterConsumer = MultiWordParameterConsumer.class,
            paramLabel = "\"NAME\"",
            description = "Second drug name (supports multi-word values).")
    String secondDrugInput;

    @Option(names = {"--output", "-o"}, required = true,
            paramLabel = "PATH",
            description = "Output path for the generated report.")
    Path output;


    @Override
    public void run() {
        try {
            ValidationUtils.validateDifferentDrugs(firstDrugInput, secondDrugInput);
            ValidationUtils.validateOutputPath(output);

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

            System.out.println("\u001B[32;1m✅ Analysis completed successfully!\u001B[0m");
            System.out.println("Output saved to: " + output);

        } catch (IllegalArgumentException e) {
            throw new CommandLine.ExecutionException(new CommandLine(this), e.getMessage());
        } catch (Exception e) {
            throw new CommandLine.ExecutionException(new CommandLine(this), "Unexpected error: " + e.getMessage());
        }


    }
}

