package nl.bioinf.logic;

import nl.bioinf.models.Combination;
import nl.bioinf.models.Drug;
import nl.bioinf.models.Interaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class InteractionCheckerTest {

    private InteractionChecker checker;

    private List<Drug> drugs;
    private List<Interaction> interactions;
    private List<Combination> combinations;

    private final String clonidine = "clonidine";
    private final String compro    = "compro";

    @BeforeEach
    void setUp() {
        // Let op volgorde: (name, id)
        drugs = List.of(
                new Drug("clonidine", "D1"),
                new Drug("compro",    "D2")
        );

        // Let op volgorde: (gene, type, score, drugId)
        interactions = List.of(
                // clonidine (D1)
                new Interaction("CYP2C9",  "agonist", "0.009349657", "D1"),
                new Interaction("CYP2D6",  "agonist", "0.013622434", "D1"),
                new Interaction("CYP2C19", "agonist", "0.008345148", "D1"),
                new Interaction("GENE_A",  "agonist", "0.001",       "D1"),
                new Interaction("GENE_B",  "agonist", "0.002",       "D1"),

                // compro (D2) — type "NULL" zoals in je screenshot
                new Interaction("CYP2C9",  "NULL", "0.008681824", "D2"),
                new Interaction("CYP2D6",  "NULL", "0.006324701", "D2"),
                new Interaction("CYP2C19", "NULL", "0.007749066", "D2"),
                new Interaction("GENE_X",  "NULL", "0.004",       "D2"),
                new Interaction("GENE_Y",  "NULL", "0.005",       "D2"),
                new Interaction("GENE_Z",  "NULL", "0.006",       "D2")
        );

        combinations = List.of(
                new Combination("agonist", "antagonist", "contraindicated"),
                new Combination("inhibitor", "substrate", "increased toxicity")
                // geen match voor agonist + NULL → verwacht "unknown"
        );

        checker = new InteractionChecker(interactions, drugs, combinations, clonidine, compro);
    }

    @Test
    void geneOverlap_findsExpectedOverlap_andWritesOutput() {
        Set<String> overlap = checker.geneOverlap();

        Set<String> expected = Set.of("CYP2C9","CYP2D6","CYP2C19");
        assertEquals(expected, overlap);

        String out = checker.getOutputSB().toString();
        assertTrue(out.contains("==== Find overlap genes ===="));
        assertTrue(out.contains("Drug 1 input: clonidine"));
        assertTrue(out.contains("Drug 2 input: compro"));
        assertTrue(out.contains("Number of overlapping genes: 3"));
        expected.forEach(g -> assertTrue(out.contains(g)));
    }

    @Test
    void geneOverlap_throwsWhenDrugMissing() {
        InteractionChecker badChecker = new InteractionChecker(interactions,drugs, combinations, "bestaatNiet", compro);
        assertThrows(IllegalArgumentException.class,
                badChecker::geneOverlap);
    }

    @Test
    void getInteractionTypes_returnsTypesForKnownDrugs() {
        String[] types = checker.getInteractionTypes();

        // bekende drugs
        assertEquals("agonist", types[0]);
        assertEquals("NULL",    types[1]);
    }


    @Test
    void getCombinationResults_returnUnknownWhenNoTableMatch() {
        Set<String> overlap = checker.geneOverlap();
        String result = checker.getCombinationResult(overlap);

        assertEquals("Unknown", result);

        String out = checker.getOutputSB().toString();
        assertTrue(out.contains("==== Combination drugs ===="));
        assertTrue(out.contains("clonidine type: agonist"));
        assertTrue(out.contains("compro type: NULL"));
        assertTrue(out.toLowerCase().contains("combination result is unknown"));
    }

    @Test
    void getCombinationResult_skipsWhenNoOverlap() {
        String res = checker.getCombinationResult(Collections.emptySet());

        assertEquals("unknown", res.toLowerCase());
        assertTrue(checker.getOutputSB().toString().contains("No gene overlap found; skipping combination result."));
    }

    @Test
    void getInteractionScorePerGene_formatsLines_andWritesHeader() {
        Set<String> overlap = Set.of("CYP2C9","CYP2D6","CYP2C19");

        var geneScores = checker.getInteractionScorePerGene(overlap);

        assertEquals(3, geneScores.size());

        Map<String, InteractionChecker.GeneScore>  byGene = new HashMap<>();
        for (var gs : geneScores) byGene.put(gs.gene(), gs);


        // CYP2C9
        assertEquals(0.009349657f, byGene.get("CYP2C9").scoreDrug1(), 1e-7);
        assertEquals(0.008681824f, byGene.get("CYP2C9").scoreDrug2(), 1e-7);

        // CYP2D6
        assertEquals(0.013622434f, byGene.get("CYP2D6").scoreDrug1(), 1e-7);
        assertEquals(0.006324701f, byGene.get("CYP2D6").scoreDrug2(), 1e-7);

        // CYP2C19
        assertEquals(0.008345148f, byGene.get("CYP2C19").scoreDrug1(), 1e-7);
        assertEquals(0.007749066f, byGene.get("CYP2C19").scoreDrug2(), 1e-7);

        String out = checker.getOutputSB().toString();
        assertTrue(out.contains("==== Interaction scores per overlap genes ===="));
        assertTrue(out.contains("gene: first drug = first drug score, second drug = second drug score"));
    }
// hier
    @Test
    void getInteractionScorePerGene_handlesMissingScoresGracefully() {
        Set<String> awkward = Set.of("CYP2C9","MISSINGGENE");

        List<InteractionChecker.GeneScore> lines = checker.getInteractionScorePerGene(awkward);

        assertEquals(1, lines.size());
        assertEquals("CYP2C9", lines.get(0).gene());

        String out = checker.getOutputSB().toString();
        assertTrue(out.contains("CYP2C9"));
    }
}

