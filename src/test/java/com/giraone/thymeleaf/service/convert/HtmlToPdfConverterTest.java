package com.giraone.thymeleaf.service.convert;

import com.giraone.thymeleaf.common.assertions.pdf.PdfContentAssertion;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HtmlToPdfConverterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlToPdfConverterTest.class);

    private static final String SOURCE = "src/test/resources/testdata/input";
    private static final String TARGET = "target/test/output";

    @Autowired
    private HtmlToPdfConverter htmlToPdfConverter;

    @Autowired
    private Environment environment;

    @BeforeAll
    static void init() {
        if (!new File(TARGET).mkdirs()) {
            LOGGER.error("Cannot create target PDF directory for tests \"{}\"!", TARGET);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "simple/input,false"
    })
    void runGeneration(String testFileName, boolean usePdfA) throws Exception {

        File htmlFile = new File(SOURCE + File.separator + testFileName + ".html");
        File pdfFile = new File(TARGET + File.separator + testFileName + ".pdf");
        if (!pdfFile.getParentFile().exists()) {
            if (!pdfFile.getParentFile().mkdirs()) {
                throw new IllegalStateException("Cannot create output directory " + pdfFile.getParentFile());
            }
        }
        PdfCreationOptions pdfCreationOptions = new PdfCreationOptions("Test-Title", "Test-Autor", usePdfA);
        long start = System.currentTimeMillis();
        htmlToPdfConverter.generatePdfFileFromHtmlFile(htmlFile, pdfFile, pdfCreationOptions);
        long end = System.currentTimeMillis();
        System.out.println("Time = " + (end - start) + " ms");
        assertThat(pdfFile.exists()).isTrue();
        checkWithPdfBoxParsing(pdfFile);
    }

    @Test
    public void generatePdfStreamFromHtmlUrl_worksWithUrls() throws Exception {

        // Arrange
        OutputStream out = new ByteArrayOutputStream();
        String port = environment.getProperty("local.server.port");
        port = port != null ? port : "8080";
        URL localUrl = new URL("http://localhost:" + port + "/static/samples/simple.html");
        PdfCreationOptions pdfCreationOptions = new PdfCreationOptions("Test-Title", "Test-Autor", false);

        // Act
        htmlToPdfConverter.generatePdfStreamFromHtmlUrl(localUrl, out, pdfCreationOptions);

        // Assert
        String actualString = out.toString();
        assertThat(actualString).startsWith("%PDF-");
        checkWithPdfBoxParsing(actualString.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void generatePdfStreamFromHtmlUrl_worksWithStrings() throws Exception {

        // Arrange
        File outFile = File.createTempFile("pd4ml-test-", ".pdf");
        outFile.deleteOnExit();
        OutputStream out = new FileOutputStream(outFile);
        String htmlString = """
            <!DOCTYPE html>
            <html>
            <head>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
            <title>PD4ML Test</title>
            </head>
            <body>
            <h2>Text mit Umlauten äöüÄÖÜß</h2>
            </body>
            </html>
            """;
        PdfCreationOptions pdfCreationOptions = new PdfCreationOptions("Test-Title", "Test-Autor", false);

        // Act
        htmlToPdfConverter.generatePdfStreamFromHtmlString(htmlString, out, pdfCreationOptions);

        // Assert
        byte[] pdfBytes = Files.readAllBytes(outFile.toPath());
        PdfContentAssertion.assertThat(pdfBytes)
            .isValidPdf()
            .containsTextStringOnAnyPage("Text mit Umlauten äöüÄÖÜß");
        checkWithPdfBoxParsing(outFile);
    }

    //------------------------------------------------------------------------------------------------------------------

    private void checkWithPdfBoxParsing(File fileToTest) throws IOException {

        byte[] pdfBytes = Files.readAllBytes(fileToTest.toPath());
        checkWithPdfBoxParsing(pdfBytes);
    }

    private void checkWithPdfBoxParsing(byte[] pdfBytes) throws IOException {

        PDFParser parser = new PDFParser(new RandomAccessBuffer(pdfBytes));
        parser.parse();

        try (PDDocument document = parser.getPDDocument()) {
            float version = document.getVersion();
            assertThat(version).isGreaterThan(1);
            int numberOfPages = document.getNumberOfPages();
            assertThat(numberOfPages).isGreaterThan(0);
        }
    }

    @SuppressWarnings("unused") // not used yet
    private void checkWithPdfBoxValidation(File fileToTest) throws IOException {

        PreflightParser parser = new PreflightParser(fileToTest);
        parser.parse();

        try (PreflightDocument document = parser.getPreflightDocument()) {
            document.validate();
            ValidationResult result = document.getResult();
            // TODO to interpret error codes see:
            // https://github.com/apache/pdfbox/blob/trunk/preflight/src/main/java/org/apache/pdfbox/preflight/PreflightConstants.java
            for (ValidationResult.ValidationError error : result.getErrorsList()) {
                System.out.println(error.getErrorCode() + " : " + error.getDetails());
            }
            assertThat(result.isValid()).isTrue();
        }
    }
}