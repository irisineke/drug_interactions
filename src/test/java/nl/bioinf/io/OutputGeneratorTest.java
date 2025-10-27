package nl.bioinf.io;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OutputGeneratorTest {

    private Path tempFile;
    private OutputGenerator outputGenerator;

    @BeforeEach
    void setUp() throws IOException {
        // Maak tijdelijk bestand aan
        tempFile = Files.createTempFile("test-output-", ".txt");
        outputGenerator = new OutputGenerator(tempFile);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Verwijder tijdelijk bestand
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testGenerateOutput_WritesExpectedContent() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("geneA\n");
        sb.append("geneB\n");
        sb.append("geneC");

        outputGenerator.generateOutput(sb);

        List<String> writtenLines = Files.readAllLines(tempFile);

        assertEquals(3, writtenLines.size(), "Er moeten drie regels geschreven zijn");
        assertEquals("geneA", writtenLines.get(0));
        assertEquals("geneB", writtenLines.get(1));
        assertEquals("geneC", writtenLines.get(2));
    }
    

    @Test
    void testGenerateOutput_ThrowsRuntimeException_OnInvalidPath() {
        Path invalidPath = Path.of("/invalid/path/output.txt");
        OutputGenerator invalidOutputGen = new OutputGenerator(invalidPath);

        StringBuilder sb = new StringBuilder("geneX");

        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                invalidOutputGen.generateOutput(sb)
        );

        assertTrue(thrown.getMessage().contains("Error in writing output overlap genes"),
                "Foutmelding moet de juiste context bevatten");
    }
}
