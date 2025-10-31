package nl.bioinf.io;

import nl.bioinf.models.Combination;
import nl.bioinf.models.Drug;
import nl.bioinf.models.Interaction;
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
    void processCombinations_readsFromResource() throws IOException {
        Path interactions = tempDir.resolve("interactions.tsv");
        Files.writeString(interactions, "gene_claim_name\tinteraction_type\tinteraction_score\tdrug_concept_id\n");
        Path drugs = tempDir.resolve("drugs.tsv");
        Files.writeString(drugs, "drug_claim_name\tconcept_id\n");

        ReadFiles rf = new ReadFiles(interactions.toFile(), drugs.toFile());
        List<Combination> list = rf.processCombinations();

        assertFalse(list.isEmpty(), "Expected the resource file to contain combinations");
        assertNotNull(list.getFirst().drugType1());
        assertNotNull(list.getFirst().drugType2());
        assertNotNull(list.getFirst().resultaat());
    }

    @Test
    void processInteractions_throwsExceptionOnMalformedRow() throws IOException {
        // Maak tijdelijk interacties.tsv met een foute regel (te weinig kolommen)
        Path interactions = tempDir.resolve("interactions.tsv");
        String malformedContent = String.join("\n",
                "gene_claim_name\tinteraction_type\tinteraction_score\tdrug_concept_id",
                "TP53\tinhibitor\t0.87", // <-- mist drug_concept_id
                "EGFR\tactivator\t0.45\tCHEMBL:999"
        );
        Files.writeString(interactions, malformedContent);


        Path drugs = tempDir.resolve("drugs.tsv");
        Files.writeString(drugs, "drug_claim_name\tconcept_id\n");

        ReadFiles rf = new ReadFiles(interactions.toFile(), drugs.toFile());

        // Controleer dat er een IllegalArgumentException wordt gegooid
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, rf::processInteractions);

        // Controleer of de foutmelding duidelijk is
        assertTrue(thrown.getMessage().contains("Malformed row"),
                "Foutmelding moet 'Malformed row' vermelden om duidelijk te zijn voor de gebruiker");
    }
}



