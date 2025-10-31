package nl.bioinf.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * The {@code OutputGenerator} class is responsible for writing the program output
 * to a specified file. It supports both plain text (.txt) and PDF (.pdf) formats.
 */

public class OutputGenerator {
    private final Path output;

    public OutputGenerator(Path output) {
        this.output = output;
    }


    public void generateOutput(StringBuilder stringBuilder) {
        String fileName = output.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".txt")) {
            generateTxt(stringBuilder);
        } else
            generatePdf(stringBuilder);
    }

    private void generateTxt(StringBuilder strBuilder) {
        try {
            List<String> lines;
            try (var reader = new java.io.BufferedReader(new java.io.StringReader(strBuilder.toString()))) {
                lines = reader.lines().toList();
            }

            Files.write(output, lines);
            System.out.println(" Text file successfully written to: " + output);
        } catch (IOException e) {
            throw new RuntimeException("Error writing text output to: " + output, e);
        }
    }

    private void generatePdf(StringBuilder stringBuilder) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(output.toFile()));
            document.open();
            document.add(new Paragraph(stringBuilder.toString()));
            System.out.println(" PDF file successfully written to: " + output);
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Error writing PDF output to: " + output, e);
        } finally {
            document.close();
        }
    }
}