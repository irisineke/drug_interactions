package nl.bioinf.logic;

import nl.bioinf.models.Combination;
import nl.bioinf.models.Drug;
import nl.bioinf.models.Interaction;
import nl.bioinf.io.CombinationScoreEffect;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class InteractionChecker {
    private final List<Interaction> interactions;
    private final List<Drug> drugs;
    private final List<Combination> combinations;
    private final String firstDrugInput;
    private final String secondDrugInput;
    private final StringBuilder outputSB;

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

    public StringBuilder getOutputSB() {
        return outputSB;
    }


    private String getConceptID(String drugInput) {
        return drugs.stream()
                .filter(drug -> drug.drugClaimName().equalsIgnoreCase(drugInput))
                .map(Drug::conceptId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Drug not found: " + drugInput));
    }

    public Set<String> geneOverlap() {

        String idDrug1 = getConceptID(firstDrugInput);
        String idDrug2 = getConceptID(secondDrugInput);


        outputSB.append("==== Find overlap genes ==== \n");
        outputSB.append("Drug 1 input: ").append(firstDrugInput).append("\n");
        outputSB.append("Drug 2 input: ").append(secondDrugInput).append("\n\n");


// get genes that influence the drugs:
        Set<String> genesDrug1 = interactions.stream()
                .filter(interaction -> interaction.drugConceptId().equals(idDrug1))
                .map(Interaction::geneClaimName)
                .collect(Collectors.toSet());

        Set<String> genesDrug2 = interactions.stream()
                .filter(interaction -> interaction.drugConceptId().equals(idDrug2))
                .map(Interaction::geneClaimName)
                .collect(Collectors.toSet());

// find overlap between drugs:
        Set<String> overlap = genesDrug1.stream()
                .filter(genesDrug2::contains)
                .collect(Collectors.toSet());

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


    public String[] getInteractionTypes() {

        String idDrug1 = getConceptID(firstDrugInput);
        String idDrug2 = getConceptID(secondDrugInput);


        String typeDrug1 = interactions.stream()
                .filter(interaction -> interaction.drugConceptId().equals(idDrug1))
                .map(Interaction::interactionType)
                .findFirst()
                .orElse("Unknown");

        String typeDrug2 = interactions.stream()
                .filter(interaction -> interaction.drugConceptId().equals(idDrug2))
                .map(Interaction::interactionType)
                .findFirst()
                .orElse("Unknown");


        return new String[]{typeDrug1, typeDrug2};
    }


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

    public record GeneScore(String gene, float scoreDrug1, float scoreDrug2) {}

    public List<GeneScore> getInteractionScorePerGene(Set<String> overlap) {

        String idDrug1 = getConceptID(firstDrugInput);
        String idDrug2 = getConceptID(secondDrugInput);
        System.out.println(firstDrugInput);

        outputSB.append("==== Interaction scores per overlap genes ====\n");

        // retrieve scores for the first drug and creates a map<gene,score>
        var scoreDrug1 = interactions.stream()
                .filter(interaction -> interaction.drugConceptId().equals(idDrug1))
                .collect(Collectors.toMap(
                        Interaction::geneClaimName,
                        interaction -> Float.parseFloat(interaction.interactionScore()),
                        (a, b) -> a)); // if gene occurs more often it keeps first score


        var scoreDrug2 = interactions.stream()
                .filter(interaction -> interaction.drugConceptId().equals(idDrug2))
                .collect(Collectors.toMap(
                        Interaction::geneClaimName,
                        interaction -> Float.parseFloat(interaction.interactionScore()),
                        (a, b) -> a));


        List<GeneScore> geneScores = overlap.stream()
                .filter(gene -> scoreDrug1.containsKey(gene) && scoreDrug2.containsKey(gene))
                // for each gene it takes scores from drug 1/2 and creates a map
                .map(gene -> new GeneScore(gene, scoreDrug1.get(gene), scoreDrug2.get(gene)))
                .toList();


        if (geneScores.isEmpty()) {
            outputSB.append("No overlapping genes with scores found.\n\n");
        } else {
            outputSB.append("gene: first drug = first drug score, second drug = second drug score\n\n");
            geneScores.forEach(geneScore -> outputSB.append(geneScore.gene())
                    .append("; ").append(firstDrugInput).append(" = ").append(geneScore.scoreDrug1())
                    .append("; ").append(secondDrugInput).append(" = ").append(geneScore.scoreDrug2())
                    .append("\n"));
        }
    outputSB.append("\n");
        return geneScores;
    }



    public String compareInteractionScore(String combinationResult,
                                                 List<GeneScore> geneScores,
                                                 Set<String> overlap) {
        outputSB.append("==== Calculating combined interaction scores ====\n");
        if (overlap.isEmpty()) {
            outputSB.append("No gene overlap found; skipping calculation.").append("\n\n");
            return "unknown";
        }
        CombinationScoreEffect effectSymbol = CombinationScoreEffect.fromResult(combinationResult);


        List<String> combinedResults = geneScores.stream()
                .map(genescore -> {
                    float combinedScore;
                    switch (effectSymbol) {
                        case ENHANCING -> {
                            combinedScore = genescore.scoreDrug1() + genescore.scoreDrug2();
                            return genescore.gene() + ": " + genescore.scoreDrug1() + " + " + genescore.scoreDrug2() + " = " + combinedScore;
                        }
                        case OPPOSING -> {
                            combinedScore = genescore.scoreDrug1() - genescore.scoreDrug2();
                            return genescore.gene() + ": " + genescore.scoreDrug1() + " - " + genescore.scoreDrug2() + " = " + combinedScore;
                        }
                        case SYNERGETISCH -> {
                            combinedScore = genescore.scoreDrug1() + genescore.scoreDrug2();
                            return genescore.gene() + ": " + genescore.scoreDrug1() + " + " + genescore.scoreDrug2() + "(synergetic) = " + combinedScore;
                        }
                        case UNKNOWN -> {
                            float plus = genescore.scoreDrug1() + genescore.scoreDrug2();
                            float minus = genescore.scoreDrug1() - genescore.scoreDrug2();
                            return genescore.gene() + ": " + genescore.scoreDrug1() + " + " + genescore.scoreDrug2() + " = " + plus + "\n\t " +
                                    genescore.scoreDrug1() + " - " + genescore.scoreDrug2() + " = " + minus;
                        }
                    }
                    return "something went wrong";
                })
                .toList();

        List<String> explanationLines = geneScores.stream()
                .map(genescore -> {
                    float combinedScore;
                    switch (effectSymbol) {
                        case ENHANCING -> {
                            float combined = genescore.scoreDrug1() + genescore.scoreDrug2();
                            return "The activity of " + genescore.gene() + " is increased by " + combined + ".";
                        }
                        case OPPOSING -> {
                            float combined = genescore.scoreDrug1() - genescore.scoreDrug2();
                            return "The activity of " + genescore.gene() + " is decreased by " + combined + ".";
                        }
                        case SYNERGETISCH -> {
                            float combined = genescore.scoreDrug1() + genescore.scoreDrug2();
                            return "The activity of " + genescore.gene() + " is increased by more than" + combined + ".";
                        }
                        case UNKNOWN -> {
                            float plus = genescore.scoreDrug1() + genescore.scoreDrug2();
                            float minus = genescore.scoreDrug1() - genescore.scoreDrug2();
                            return "The activity of " + genescore.gene() + " is increased by " + plus + " or decreased by " + minus + ".";

                        }
                    }
                    return "something went wrong";
                })
                .toList();


        combinedResults.forEach(line -> outputSB.append(line).append("\n"));
        outputSB.append("\n");
        outputSB.append("==== Calculation Results ==== \n");
        explanationLines.forEach(line -> outputSB.append(line).append("\n"));
        return "done";
    }
}