package nl.bioinf.io;

import nl.bioinf.methods.Combination;
import nl.bioinf.methods.Drug;
import nl.bioinf.methods.Interaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReadFilesTest {

    @TempDir
    Path tempDir;

    Path createdCombinationsPath = null;

    @AfterEach
    void cleanupCombinations() throws IOException {
        if (createdCombinationsPath != null) {
            Files.deleteIfExists(createdCombinationsPath);
            Path dataDir = createdCombinationsPath.getParent();
            if (dataDir != null && Files.isDirectory(dataDir)) {
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(dataDir)) {
                    if (!ds.iterator().hasNext()) Files.deleteIfExists(dataDir);
                }
            }
        }
    }

    @Test
    void processInteractions_readsValidRows() throws IOException {
        Path interactions = tempDir.resolve("interactions.tsv");
        String content = String.join("\n",
                "gene_claim_name\tinteraction_type\tinteraction_score\tdrug_concept_id",
                "TP53\tinhibitor\t0.87\tCHEMBL:123",
                "",
                "EGFR\tactivator\t0.45\tCHEMBL:999"
        );
        Files.writeString(interactions, content);

        Path drugs = tempDir.resolve("drugs.tsv");
        Files.writeString(drugs, "drug_claim_name\tconcept_id\n");

        ReadFiles rf = new ReadFiles(interactions.toFile(), drugs.toFile());
        List<Interaction> list = rf.processInteractions();

        assertEquals(2, list.size());
        assertEquals("TP53", list.getFirst().geneClaimName());
        assertEquals("inhibitor", list.getFirst().interactionType());
        assertEquals("0.87", list.getFirst().interactionScore());
        assertEquals("CHEMBL:123", list.getFirst().drugConceptId());
    }

    @Test
    void processDrugs_readsValidRows() throws IOException {
        Path interactions = tempDir.resolve("interactions.tsv");
        Files.writeString(interactions,
                "gene_claim_name\tinteraction_type\tinteraction_score\tdrug_concept_id\n");

        Path drugs = tempDir.resolve("drugs.tsv");
        String content = String.join("\n",
                "drug_claim_name\tconcept_id",
                "Imatinib\tCHEMBL:1544",
                "Gefitinib\tCHEMBL:115"
        );
        Files.writeString(drugs, content);

        ReadFiles rf = new ReadFiles(interactions.toFile(), drugs.toFile());
        List<Drug> list = rf.processDrugs();

        assertEquals(2, list.size());
        assertEquals("Imatinib", list.getFirst().drugClaimName());
        assertEquals("CHEMBL:1544", list.getFirst().conceptId());
    }

    @Test
    void processCombinations_readsFromFixedDataPath() throws IOException {
        Path dataDir = Paths.get("data");
        Files.createDirectories(dataDir);
        Path combinations = dataDir.resolve("drug_combinations.tsv");
        createdCombinationsPath = combinations;

        String content = String.join("\n",
                "drugtype_1\tdrugtype_2\tresultaat",
                "TKI\tChemo\tSynergistic",
                "Hormonal\tTKI\tAntagonistic"
        );
        Files.writeString(combinations, content);

        Path interactions = tempDir.resolve("interactions.tsv");
        Files.writeString(interactions,
                "gene_claim_name\tinteraction_type\tinteraction_score\tdrug_concept_id\n");
        Path drugs = tempDir.resolve("drugs.tsv");
        Files.writeString(drugs, "drug_claim_name\tconcept_id\n");

        ReadFiles rf = new ReadFiles(interactions.toFile(), drugs.toFile());
        List<Combination> list = rf.processCombinations();

        assertEquals(2, list.size());
        assertEquals("TKI", list.getFirst().drugType1());
        assertEquals("Chemo", list.getFirst().drugType2());
        assertEquals("Synergistic", list.getFirst().resultaat());
    }

    @Test
    void emptyFiles_returnEmptyLists() throws IOException {
        Path interactions = tempDir.resolve("empty_interactions.tsv");
        Path drugs = tempDir.resolve("empty_drugs.tsv");
        Files.writeString(interactions, "");
        Files.writeString(drugs, "");

        ReadFiles rf = new ReadFiles(interactions.toFile(), drugs.toFile());
        assertTrue(rf.processInteractions().isEmpty());
        assertTrue(rf.processDrugs().isEmpty());
    }
}



