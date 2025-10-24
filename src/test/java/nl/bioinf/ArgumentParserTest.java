package nl.bioinf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentParserTest {

    @TempDir
    Path tempDir;

    @Test
    void fileNotEmptyCheck_throwsWhenFileDoesNotExist() {
        ArgumentParser ap = new ArgumentParser();
        Path missing = tempDir.resolve("missing.tsv");
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ap.fileNotEmptyCheck("Interactions file", missing.toString())
        );
        assertTrue(ex.getMessage().contains("does not exist"));
    }

    @Test
    void fileNotEmptyCheck_throwsWhenPathIsDirectory() throws IOException {
        ArgumentParser ap = new ArgumentParser();
        Path dir = tempDir.resolve("aDir");
        Files.createDirectory(dir);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ap.fileNotEmptyCheck("Drugs file", dir.toString())
        );
        assertTrue(ex.getMessage().contains("is not a file"));
    }

    @Test
    void fileNotEmptyCheck_throwsWhenFileIsEmpty() throws IOException {
        ArgumentParser ap = new ArgumentParser();
        Path empty = tempDir.resolve("empty.tsv");
        Files.createFile(empty); // lege file

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ap.fileNotEmptyCheck("Interactions file", empty.toString())
        );
        assertTrue(ex.getMessage().contains("is empty"));
    }

    @Test
    void fileNotEmptyCheck_okWhenFileHasContent() throws IOException {
        ArgumentParser ap = new ArgumentParser();
        Path ok = tempDir.resolve("ok.tsv");
        Files.writeString(ok, "header1\theader2\nvalue\tvalue\n"); // heeft content

        assertDoesNotThrow(() ->
                ap.fileNotEmptyCheck("Drugs file", ok.toString())
        );
    }

    @Test
    void picocli_parsesOptionsIntoFields_withoutRunning() throws IOException {
        // Maak geldige fake files (inhoud maakt niet uit voor parsing)
        Path interactions = tempDir.resolve("interactions.tsv");
        Files.writeString(interactions, "x\n");
        Path drugs = tempDir.resolve("drugs.tsv");
        Files.writeString(drugs, "x\n");
        Path out = tempDir.resolve("out.txt");

        ArgumentParser ap = new ArgumentParser();
        String[] args = new String[] {
                "-intF", interactions.toString(),
                "-drF", drugs.toString(),
                "--drug1", "aspirin",
                "--drug2", "ibuprofen",
                "--output", out.toString()
        };
        new CommandLine(ap).parseArgs(args); // vult de velden, voert run() NIET uit

        // Controleer dat velden goed zijn gezet
        assertEquals(interactions.toFile().getAbsolutePath(), ap.interactionsFile.getAbsolutePath());
        assertEquals(drugs.toFile().getAbsolutePath(), ap.drugsFile.getAbsolutePath());
        assertEquals("aspirin", ap.firstDrugInput);
        assertEquals("ibuprofen", ap.secondDrugInput);
        assertEquals(out, ap.output);
    }
}
