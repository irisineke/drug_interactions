package nl.bioinf.logic;

import nl.bioinf.models.Combination;
import nl.bioinf.models.Drug;
import nl.bioinf.models.Interaction;
import nl.bioinf.io.CombinationScoreEffect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

/**
 * InteractionChecker is responsible for analyzing interactions between two drugs,
 * finding overlapping genes, computing interaction types and scores, and generating
 * a detailed output report.
 */
public class InteractionChecker {
    private final List<Interaction> interactions;
    private final List<Drug> drugs;
    private final List<Combination> combinations;
    private final String firstDrugInput;
    private final String secondDrugInput;
    private final StringBuilder outputSB;

    /**
     * Constructs an InteractionChecker instance with the given data.
     *
     * @param interactions List of known drug-gene interactions.
     * @param drugs List of drugs names.
     * @param combinations List of drug combinations with interaction results.
     * @param firstDrugInput Name of the first drug for comparison.
     * @param secondDrugInput Name of the second drug for comparison.
     */
    public InteractionChecker (List<Interaction> interactions,
                               List<Drug> drugs,
                               List<Combination> combinations,
                               String firstDrugInput,
                               String secondDrugInput) {
        this.drugs = drugs;
        this.interactions = interactions;
        this.combinations = combinations;
        this.firstDrugInput = firstDrugInput;
        this.secondDrugInput = secondDrugInput;
        this.outputSB = new StringBuilder();
    }

    /**
     * Returns the StringBuilder containing the generated output report.
     *
     * @return StringBuilder with message of the analysis.
     */
    public StringBuilder getOutputSB() {
        return outputSB;
    }

    /**
     * returns the concept ID for a given drug input name.
     * 
     * @param drugInput The name of the drug.
     * @return The concept ID of the drug.
     * @throws IllegalArgumentException if the drug is not found.
     */
    private String getConceptID(String drugInput) {
        return drugs.stream()
                .filter(drug -> drug.drugClaimName().equalsIgnoreCase(drugInput))
                .map(Drug::conceptId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Drug not found: " + drugInput));
    }

    /**
     * Finds genes by both drugs and returns the overlapping genes.
     * 
     * @return Set of gene names that are influenced by both drugs.
     */
    public Set<String> geneOverlap() {

        String idDrug1 = getConceptID(firstDrugInput);
        String idDrug2 = getConceptID(secondDrugInput);


        outputSB.append("==== Find overlap genes ==== \n");
        outputSB.append("Drug 1 input: ").append(firstDrugInput).append("\n");
        outputSB.append("Drug 2 input: ").append(secondDrugInput).append("\n\n");


// get genes that influence the drugs:
        Set<String> genesDrug1 = new HashSet<>();
        for (Interaction interaction : interactions) {
            if (interaction.drugConceptId().equalsIgnoreCase(idDrug1)) {
                genesDrug1.add(interaction.geneClaimName());
            }
        }

        Set<String> genesDrug2 = new HashSet<>();
        for (Interaction interaction : interactions) {
            if (interaction.drugConceptId().equalsIgnoreCase(idDrug2)) {
                genesDrug2.add(interaction.geneClaimName());
            }
        }

// find overlap between drugs:
        Set<String> overlap = new HashSet<>();
        for (String gene : genesDrug1) {
            if (genesDrug2.contains(gene)) {
                overlap.add(gene);
            }
        }

        outputSB.append("Number of genes influenced by ").append(firstDrugInput).append(": ").append(genesDrug1.size()).append("\n");
        outputSB.append("Number of genes influenced by ").append(secondDrugInput).append(": ").append(genesDrug2.size()).append("\n\n");


        if (overlap.isEmpty()) {
            outputSB.append("No overlap found.\n");
        } else {
            outputSB.append("Number of overlapping genes: ").append(overlap.size()).append("\n");
            outputSB.append("Overlapping genes: ").append("\n");
            overlap.forEach(gene -> outputSB.append(gene).append("\n"));
        }

        outputSB.append("\n");
        return overlap;
    }


    /**
     * Returns the interaction types of the two drugs.
     * 
     * @return Array of Strings, first element is the type of the first drug, second element is the type of the second drug.
     */
    public String[] getInteractionTypes() {

        String idDrug1 = getConceptID(firstDrugInput);
        String idDrug2 = getConceptID(secondDrugInput);

        String typeDrug1 = "Unknown";
        String typeDrug2 = "Unknown";

        for (Interaction interaction : interactions) {
            if (interaction.drugConceptId().equals(idDrug1)) {
                typeDrug1 = interaction.interactionType();
            }

            if (interaction.drugConceptId().equals(idDrug2)) {
                typeDrug2 = interaction.interactionType();
            }
        }
        return new String[]{typeDrug1, typeDrug2};
}


    /**
     * Determines the combination result based on overlapping genes and interaction types.
     * 
     * @param overlap Set of overlapping genes.
     * @return The result of the combination ("enhancing", "opposing", etc.) or "unknown".
     */
    public String getCombinationResult(Set<String> overlap) {
        outputSB.append("==== Combination drugs ==== \n");
        if (overlap.isEmpty()) {
            outputSB.append("No gene overlap found; skipping combination result.").append("\n\n");
            return "unknown";
        }

        String[] types = getInteractionTypes();
        String typeDrug1 = types[0];
        String typeDrug2 = types[1];

        outputSB.append(firstDrugInput).append(" type: ").append(typeDrug1).append("\n");
        outputSB.append(secondDrugInput).append(" type: ").append(typeDrug2).append("\n\n");


        // searches the types in the drug_combination.tsv and returns the result (column with combination result)
        for (Combination comb : combinations) {
            boolean match = comb.drugType1().equalsIgnoreCase(typeDrug1) && comb.drugType2().equalsIgnoreCase(typeDrug2) ||
                    comb.drugType1().equalsIgnoreCase(typeDrug2) && comb.drugType2().equalsIgnoreCase(typeDrug1);

            if (match) {
                outputSB.append("Combination result: ").append(comb.resultaat()).append("\n\n");
                return comb.resultaat();
            }
        }


        outputSB.append("Combination result is unknown").append("\n\n");
        return "Unknown";
    }

    /**
     * Represents a gene and its interaction score for the two drugs.
     * 
     * @param gene
     * @param scoreDrug1
     * @param scoreDrug2
     */
    public record GeneScore(String gene, float scoreDrug1, float scoreDrug2) {}

    /**
     * Retrieves the interaction score per overlapping gene for the two drugs.
     * 
     * @param overlap Set of overlapping genes.
     * @return List of GeneScore objects containing gene names and corresponding scores.
     */
    public List<GeneScore> getInteractionScorePerGene(Set<String> overlap) {

        String idDrug1 = getConceptID(firstDrugInput);
        String idDrug2 = getConceptID(secondDrugInput);

        outputSB.append("==== Interaction scores per overlap genes ====\n");


        Map<String, Float> scoreDrug1 = new HashMap<>();
        Map<String, Float> scoreDrug2 = new HashMap<>();


        for (Interaction interaction : interactions) {

            String drugId = interaction.drugConceptId();
            String gene = interaction.geneClaimName();

            float score;
            try {
                score = Float.parseFloat(interaction.interactionScore());
            } catch (NumberFormatException e) {

                continue;
            }


            if (drugId.equals(idDrug1)) {
                if (!scoreDrug1.containsKey(gene)) {
                    scoreDrug1.put(gene, score);
                }
            }

            if (drugId.equals(idDrug2)) {
                if (!scoreDrug2.containsKey(gene)) {
                    scoreDrug2.put(gene, score);
                }
            }
        }


        List<GeneScore> geneScores = new ArrayList<>();

        for (String gene : overlap) {
            if (scoreDrug1.containsKey(gene) && scoreDrug2.containsKey(gene)) {
                geneScores.add(new GeneScore(gene, scoreDrug1.get(gene), scoreDrug2.get(gene)));
            }
        }


        if (geneScores.isEmpty()) {
            outputSB.append("No overlapping genes with scores found.\n\n");
        } else {
            outputSB.append("gene: first drug = first drug score, second drug = second drug score\n\n");
            for (GeneScore geneScore : geneScores) {
                outputSB.append(geneScore.gene())
                        .append("; ").append(firstDrugInput).append(" = ").append(geneScore.scoreDrug1())
                        .append("; ").append(secondDrugInput).append(" = ").append(geneScore.scoreDrug2())
                        .append("\n");
            }
        }

        outputSB.append("\n");
        return geneScores;
}



    /**
     * Calculates combined interaction scores based on the combination result and gene scores.
     * 
     * @param combinationResult Result of drug combination.
     * @param geneScores List of GeneScore objects for overlapping genes.
     * @param overlap Set of overlapping genes.
     * @return Status string ("done" or "unknown") after calculation.
     */
    public String compareInteractionScore(String combinationResult,
                                                 List<GeneScore> geneScores,
                                                 Set<String> overlap) {
        outputSB.append("==== Calculating combined interaction scores ====\n");
        if (overlap.isEmpty()) {
            outputSB.append("No gene overlap found; skipping calculation.").append("\n\n");
            return "unknown";
        }
        CombinationScoreEffect effectSymbol = CombinationScoreEffect.fromResult(combinationResult);


        List<String> combinedResults = new ArrayList<>();

        for (GeneScore genescore : geneScores) {
            float score1 = genescore.scoreDrug1();
            float score2 = genescore.scoreDrug2();
            float combinedScore;

            switch (effectSymbol) {
                case ENHANCING: {
                    combinedScore = score1 + score2;
                    combinedResults.add(genescore.gene() + ": " + score1 + " + " + score2 + " = " + combinedScore);
                    break;
                }
                case OPPOSING: {
                    combinedScore = score1 - score2;
                    combinedResults.add(genescore.gene() + ": " + score1 + " - " + score2 + " = " + combinedScore);
                    break;
                }
                case SYNERGETISCH: {
                    combinedScore = score1 + score2;
                    combinedResults.add(genescore.gene() + ": " + score1 + " + " + score2 + "(synergetic) = " + combinedScore);
                    break;
                }
                case UNKNOWN: {
                    float plus = score1 + score2;
                    float minus = score1 - score2;
                    combinedResults.add(genescore.gene() + ": " + score1 + " + " + score2 + " = " + plus + "\n\t " +
                            score1 + " - " + score2 + " = " + minus);
                        break;
                }
            }
        }


        List<String> explanationLines = new ArrayList<>();

        for (GeneScore genescore : geneScores) {
            float score1 = genescore.scoreDrug1();
            float score2 = genescore.scoreDrug2();
            float combinedScore;

            switch (effectSymbol) {
                case ENHANCING: {
                    combinedScore = score1 + score2;
                    explanationLines.add("The activity of " + genescore.gene() + " is increased by " + combinedScore + ".");
                    break;
                }
                case OPPOSING: {
                    combinedScore = score1 + score2;
                    explanationLines.add("The activity of " + genescore.gene() + " is decreased by " + combinedScore + ".");
                    break;
                }
                case SYNERGETISCH: {
                    combinedScore = score1 + score2;
                    explanationLines.add("The activity of " + genescore.gene() + " is increased by more than" + combinedScore + ".");
                    break;
                }
                case UNKNOWN: {
                    float plus = score1 + score2;
                    float minus = score1 - score2;
                    explanationLines.add("The activity of " + genescore.gene() + " is increased by " + plus + " or decreased by " + minus + ".");
                    break;
                }
            }
        }


        // output:
        for (String line : combinedResults) {
            outputSB.append(line).append("\n");
        }

        outputSB.append("\n");
        outputSB.append("==== Calculation Results ==== \n");

        for (String line : explanationLines) {
            outputSB.append(line).append("\n");
        }
        return "done";
    }
}