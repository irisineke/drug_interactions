package nl.bioinf.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validate")
class ValidationUtilsTest {

    @TempDir
    Path tmp;

    // ---------- validateOutputPath ----------

    @Nested
    @DisplayName("validateOutputPath(Path)")
    class OutputPathTests {

        @Test
        @DisplayName("accepts .txt and .pdf in existing, writable directory")
        void acceptsTxtAndPdf() throws IOException {
            Path dir = Files.createDirectory(tmp.resolve("out"));
            ValidationUtils.validateOutputPath(dir.resolve("report.txt"));
            ValidationUtils.validateOutputPath(dir.resolve("report.pdf"));
        }

        @Test
        @DisplayName("rejects null")
        void rejectsNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.validateOutputPath(null));
            assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("output path is null"));
        }

        @Test
        @DisplayName("rejects unsupported extension")
        void rejectsUnsupportedExtension() throws IOException {
            Path dir = Files.createDirectory(tmp.resolve("out"));
            Path out = dir.resolve("report.csv");
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.validateOutputPath(out));
            assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("must end with '.txt' or '.pdf'"));
        }

        @Test
        @DisplayName("rejects empty basename (like '.txt')")
        void rejectsEmptyBasename() throws IOException {
            Path dir = Files.createDirectory(tmp.resolve("out"));
            Path out = dir.resolve(".txt");
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.validateOutputPath(out));
            assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("must include a name before the extension"));
        }

        @Test
        @DisplayName("rejects non-existent directory")
        void rejectsNonExistentDirectory() {
            Path out = tmp.resolve("no_such_dir").resolve("report.txt");
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.validateOutputPath(out));
            assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("directory does not exist"));
        }

        @Test
        @DisplayName("rejects when parent is not a directory")
        void rejectsParentNotDirectory() throws IOException {
            Path fileAsParent = tmp.resolve("not_a_dir");
            Files.write(fileAsParent, List.of("x"));
            Path out = fileAsParent.resolve("report.txt"); // resolving against a file is okay syntactisch
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.validateOutputPath(out));
            assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("parent is not a directory"));
        }

        @Test
        @DisplayName("rejects non-writable directory (POSIX only)")
        void rejectsNonWritableDirectory_posixOnly() throws IOException {
            // Alleen draaien als POSIX permissions ondersteund worden
            Assumptions.assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));

            Path dir = Files.createDirectory(tmp.resolve("readonly"));
            // verwijder write-permission
            var perms = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(dir, perms);

            Path out = dir.resolve("report.txt");
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.validateOutputPath(out));
            assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("not writable"));
        }
    }

    // ---------- validateDifferentDrugs ----------

    @Nested
    @DisplayName("validateDifferentDrugs(String, String)")
    class DifferentDrugsTests {
        @Test
        @DisplayName("accepts different names (case-insensitive)")
        void acceptsDifferent() {
            ValidationUtils.validateDifferentDrugs("Aspirin", "Ibuprofen");
        }

        @Test
        @DisplayName("rejects null values")
        void rejectsNulls() {
            assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateDifferentDrugs(null, "X"));
            assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateDifferentDrugs("X", null));
        }

        @Test
        @DisplayName("rejects same names ignoring case")
        void rejectsSameIgnoringCase() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.validateDifferentDrugs("Clonidine", "clonidine"));
            assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("same drug"));
        }
    }

    // ---------- validateTsvFile / validateTsvPath ----------

    @Nested
    @DisplayName("validateTsvFile/Path")
    class TsvValidationTests {

        @Test
        @DisplayName("accepts normal .tsv file")
        void acceptsValidTsv() throws IOException {
            Path tsv = tmp.resolve("ok.tsv");
            Files.write(tsv, List.of("header1\theader2", "a\tb"));
            ValidationUtils.validateTsvPath(tsv, "Drugs TSV");
            ValidationUtils.validateTsvFile(tsv.toFile(), "Drugs TSV");
        }

        @Test
        @DisplayName("rejects null")
        void rejectsNull() {
            assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateTsvPath(null, "X"));
            assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateTsvFile( null, "X"));
        }

        @Test
        @DisplayName("rejects missing file")
        void rejectsMissingFile() {
            Path missing = tmp.resolve("missing.tsv");
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.validateTsvPath(missing, "Interactions TSV"));
            assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("not found"));
        }

        @Test
        @DisplayName("rejects directory instead of file")
        void rejectsDirectory() throws IOException {
            Path dir = Files.createDirectory(tmp.resolve("dir_as_file"));
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.validateTsvPath(dir, "Interactions TSV"));
            assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("is not a file"));
        }

        @Test
        @DisplayName("rejects empty file")
        void rejectsEmptyFile() throws IOException {
            Path empty = Files.createFile(tmp.resolve("empty.tsv"));
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.validateTsvPath(empty, "Interactions TSV"));
            assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("is empty"));
        }

        @Test
        @DisplayName("rejects non-.tsv extension")
        void rejectsNonTsvExtension() throws IOException {
            Path csv = tmp.resolve("data.csv");
            Files.write(csv, List.of("a,b"));
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.validateTsvPath(csv, "Interactions TSV"));
            assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("must be a .tsv file"));
        }

        @Test
        @DisplayName("rejects unreadable file (POSIX only)")
        void rejectsUnreadable_posixOnly() throws IOException {
            Assumptions.assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));

            Path f = Files.write(tmp.resolve("unreadable.tsv"), List.of("a\tb"));
            Files.setPosixFilePermissions(f, EnumSet.noneOf(PosixFilePermission.class));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.validateTsvPath(f, "Drugs TSV"));
            assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("not readable"));
        }
    }

    // ---------- helper factories (ioWriteError, ioReadError, unsupportedOutputFormat) ----------

    @Nested
    @DisplayName("Helper factories")
    class HelperFactoriesTests {
        @Test
        @DisplayName("unsupportedOutputFormat builds informative message")
        void unsupportedOutputFormat_hasMessage() {
            Path p = Paths.get("/tmp/out.xyz");
            IllegalArgumentException ex = ValidationUtils.unsupportedOutputFormat(p);
            assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("must end with '.txt' or '.pdf'"));
        }

        @Test
        @DisplayName("ioWriteError wraps cause and path")
        void ioWriteError_hasPathAndCause() {
            Path p = Paths.get("/tmp/out.txt");
            IOException cause = new IOException("disk full");
            IllegalArgumentException ex = ValidationUtils.ioWriteError(p, cause);
            assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("failed to write"));
            assertSame(cause, ex.getCause());
        }

        @Test
        @DisplayName("ioReadError wraps cause and path")
        void ioReadError_hasPathAndCause() {
            Path p = Paths.get("/tmp/in.tsv");
            IOException cause = new IOException("permission denied");
            IllegalArgumentException ex = ValidationUtils.ioReadError(p, cause);
            assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("failed to read"));
            assertSame(cause, ex.getCause());
        }
    }
}
