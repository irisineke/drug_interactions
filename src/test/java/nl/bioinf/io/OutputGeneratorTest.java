package nl.bioinf.io;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OutputGenerator")
class OutputGeneratorTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("Text output (.txt)")
    class TextOutputTests {

        @Test
        @DisplayName("writes expected lines to .txt")
        void writesExpectedContent() throws IOException {
            Path out = tempDir.resolve("result.txt");
            OutputGenerator gen = new OutputGenerator(out);

            StringBuilder sb = new StringBuilder()
                    .append("geneA\n")
                    .append("geneB\n")
                    .append("geneC");

            gen.generateOutput(sb);

            List<String> lines = Files.readAllLines(out);
            assertEquals(3, lines.size(), "There should be three lines");
            assertEquals("geneA", lines.get(0));
            assertEquals("geneB", lines.get(1));
            assertEquals("geneC", lines.get(2));
        }

        @Test
        @DisplayName("handles empty StringBuilder (creates empty or single-blank line file)")
        void handlesEmptyBuilder() throws IOException {
            Path out = tempDir.resolve("empty.txt");
            OutputGenerator gen = new OutputGenerator(out);

            gen.generateOutput(new StringBuilder());

            assertTrue(Files.exists(out), "File should be created");
            // Implementations may write an empty file OR one empty line.
            long size = Files.size(out);
            List<String> lines = Files.readAllLines(out);
            boolean ok = (size == 0) || (lines.size() == 1 && lines.get(0).isEmpty());
            assertTrue(ok, "Empty output should result in an empty file or a single empty line");
        }
    }

    @Nested
    @DisplayName("PDF output (.pdf)")
    class PdfOutputTests {

        @Test
        @DisplayName("creates a non-empty .pdf file")
        void createsNonEmptyPdf() throws IOException {
            Path out = tempDir.resolve("report.pdf");
            OutputGenerator gen = new OutputGenerator(out);

            StringBuilder sb = new StringBuilder("This is a PDF test\nLine 2\nLine 3");
            gen.generateOutput(sb);

            assertTrue(Files.exists(out), "PDF should be created");
            assertTrue(Files.size(out) > 0, "PDF should not be empty");
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("throws IllegalArgumentException for unsupported extension")
        void throwsForUnsupportedExtension() {
            Path out = tempDir.resolve("output.csv"); // not supported
            OutputGenerator gen = new OutputGenerator(out);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> gen.generateOutput(new StringBuilder("x")));

            assertTrue(ex.getMessage().toLowerCase().contains("unsupported"),
                    "Message should indicate unsupported output format");
        }

        @Test
        @DisplayName("throws RuntimeException for invalid path")
        void throwsRuntimeException_OnInvalidPath() {
            Path invalidPath = Path.of("/invalid/path/output.txt");
            OutputGenerator gen = new OutputGenerator(invalidPath);

            RuntimeException thrown = assertThrows(RuntimeException.class,
                    () -> gen.generateOutput(new StringBuilder("geneX")));

            assertTrue(thrown.getMessage().contains("Error in writing output overlap genes")
                            || thrown.getMessage().toLowerCase().contains("error writing"),
                    "Error message should contain context about writing failure");
        }
    }
}
