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
        checker = new InteractionChecker();

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
    }

    @Test
    void geneOverlap_findsExpectedOverlap_andWritesOutput() {
        StringBuilder sb = new StringBuilder();

        Set<String> overlap = checker.geneOverlap(
                interactions, drugs, clonidine, compro, sb
        );

        Set<String> expected = Set.of("CYP2C9","CYP2D6","CYP2C19");
        assertEquals(expected, overlap);

        String out = sb.toString();
        assertTrue(out.contains("==== Find overlap genes ===="));
        assertTrue(out.contains("Drug 1 input: clonidine"));
        assertTrue(out.contains("Drug 2 input: compro"));
        assertTrue(out.contains("Number of overlapping genes: 3"));
        expected.forEach(g -> assertTrue(out.contains(g)));
    }

    @Test
    void geneOverlap_throwsWhenDrugMissing() {
        StringBuilder sb = new StringBuilder();
        assertThrows(IllegalArgumentException.class, () ->
                checker.geneOverlap(interactions, drugs, "bestaatNiet", compro, sb)
        );
    }

    @Test
    void getInteractionTypes_returnsTypesForKnownDrugs_andThrowsForUnknown() {
        String[] types = InteractionChecker.getInteractionTypes(
                interactions, drugs, clonidine, compro
        );

        // bekende drugs
        assertEquals("agonist", types[0]);
        assertEquals("NULL",    types[1]);

        // onbekende drug moet exception gooien
        assertThrows(IllegalArgumentException.class, () ->
                InteractionChecker.getInteractionTypes(interactions, drugs, clonidine, "nietbestaat")
        );
    }


    @Test
    void getCombinationResult_returnsUnknownWhenNoTableMatch() {
        StringBuilder sb = new StringBuilder();
        Set<String> overlap = checker.geneOverlap(
                interactions, drugs, clonidine, compro, new StringBuilder()
        );

        String result = InteractionChecker.getCombinationResult(
                interactions, drugs, clonidine, compro, combinations, overlap, sb
        );

        assertTrue(result.equalsIgnoreCase("unknown"));

        String out = sb.toString();
        assertTrue(out.contains("==== Find type drugs ===="));
        assertTrue(out.contains("clonidine type: agonist"));
        assertTrue(out.contains("compro type: NULL"));
        assertTrue(out.toLowerCase().contains("combination result is unknown"));
    }

    @Test
    void getCombinationResult_skipsWhenNoOverlap() {
        StringBuilder sb = new StringBuilder();

        String res = InteractionChecker.getCombinationResult(
                interactions, drugs, clonidine, compro, combinations, Collections.emptySet(), sb
        );

        assertEquals("unknown", res);
        assertTrue(sb.toString().contains("No gene overlap found; skipping combination result."));
    }

    @Test
    void getInteractionScorePerGene_formatsLines_andWritesHeader() {
        StringBuilder sb = new StringBuilder();
        Set<String> overlap = Set.of("CYP2C9","CYP2D6","CYP2C19");

        List<String> lines = InteractionChecker.GetInteractionScorePerGene(
                interactions, drugs, clonidine, compro, overlap, sb
        );

        assertEquals(3, lines.size());

        Map<String,String> byGene = lines.stream()
                .collect(Collectors.toMap(
                        l -> l.substring(0, l.indexOf(":")),
                        l -> l
                ));

        assertEquals("CYP2C9: clonidine = 0.009349657, compro = 0.008681824", byGene.get("CYP2C9"));
        assertEquals("CYP2D6: clonidine = 0.013622434, compro = 0.006324701", byGene.get("CYP2D6"));
        assertEquals("CYP2C19: clonidine = 0.008345148, compro = 0.007749066", byGene.get("CYP2C19"));

        String out = sb.toString();
        assertTrue(out.contains("=== Interaction scores per overlap genes ==="));
        assertTrue(out.contains("gene: first drug = first drug score, second drug = second drug score"));
    }

    @Test
    void getInteractionScorePerGene_handlesMissingScoresGracefully() {
        StringBuilder sb = new StringBuilder();
        Set<String> awkward = Set.of("CYP2C9","MISSINGGENE");

        List<String> lines = InteractionChecker.GetInteractionScorePerGene(
                interactions, drugs, clonidine, compro, awkward, sb
        );

        assertEquals(1, lines.size());
        assertTrue(lines.getFirst().startsWith("CYP2C9: "));
    }
}

