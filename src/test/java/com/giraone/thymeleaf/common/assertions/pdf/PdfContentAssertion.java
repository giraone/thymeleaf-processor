package com.giraone.thymeleaf.common.assertions.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.apache.pdfbox.preflight.utils.ByteArrayDataSource;
import org.apache.pdfbox.text.PDFTextStripper;
import org.assertj.core.api.AbstractAssert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

/**
 * A custom assertion class, to check whether a PDF contains a certain data, e.g. a certain text [in a certain region].
 * TODO: Extract this to a JAR, so it can be used anywhere
 * Should be used like this - here the PDF is read and parsed only once:
 * <code>
 * byte[] pdfBytes = ...
 * PdfContentAssertion.assertThat(pdfBytes)
 * .isValidPdf()
 * .containsTextStringAtPosition(1, new RectangleInMm(60, 80, 160, 120), "PDF Overlay")
 * .containsTextStringAtPosition(1, new RectangleInMm(60, 100, 160, 140), "Feld A")
 * .containsTextStringAtPosition(1, new RectangleInMm(60, 120, 160, 160), "Feld B")
 * .containsTextStringAtPosition(2, new RectangleInMm(60, 80, 160, 120), "PDF Overlay")
 * .containsTextStringAtPosition(2, new RectangleInMm(60, 100, 160, 140), "Feld C")
 * .containsTextStringAtPosition(2, new RectangleInMm(60, 120, 160, 160), "Feld D");
 * </code>
 */
public class PdfContentAssertion extends AbstractAssert<PdfContentAssertion, byte[]> {

    String allText;
    Integer actualNumberOfPages;
    List<TextLocation> locations;
    PointAdaption pointAdaption;
    ScaleAdaption scaleAdaption;

    public PdfContentAssertion(byte[] bytes, PointAdaption pointAdaption, ScaleAdaption scaleAdaption) {
        super(bytes, PdfContentAssertion.class);
        this.pointAdaption = pointAdaption;
        this.scaleAdaption = scaleAdaption;
    }

    public static PdfContentAssertion assertThat(byte[] actual) {
        return new PdfContentAssertion(actual, new PointAdaption(0.0f,0.0f), new ScaleAdaption(1.0f,1.0f));
    }

    //------------------------------------------------------------------------------------------------------------------

    public PdfContentAssertion withPointAdaption(PointAdaption pointAdaption) {
        this.pointAdaption = pointAdaption;
        return this;
    }

    public PdfContentAssertion withScaleAdaption(ScaleAdaption scaleAdaption) {
        this.scaleAdaption = scaleAdaption;
        return this;
    }

    public PdfContentAssertion isValidPdf() {

        if (this.actual == null) {
            failWithMessage("PDF bytes are null");
            return this;
        }
        if (this.actual.length < 50) {
            failWithMessage("PDF bytes are too small! Expected at least 50 bytes, but was {}", this.actual.length);
            return this;
        }
        final String magicIntro = new String(this.actual, 0, 5);
        if (!"%PDF-".equals(magicIntro)) {
            failWithMessage("PDF does not start with %PDF-, it was \"{}\"", magicIntro);
            return this;
        }
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public PdfContentAssertion isNotValidPdf() {

        if (this.actual == null) {
            return this;
        }
        if (this.actual.length < 50) {
            return this;
        }
        final String magicIntro = new String(this.actual, 0, 5);
        if (!"%PDF-".equals(magicIntro)) {
            return this;
        }
        failWithMessage("PDF seems to be valid");
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public PdfContentAssertion isValidPdfA() throws IOException {

        if (this.actual == null) {
            failWithMessage("PDF bytes are null");
            return this;
        }

        if (this.actual.length < 50) {
            failWithMessage("PDF bytes are too small! Expected at least 50 bytes, but was " + this.actual.length);
            return this;
        }

        PreflightParser parser = new PreflightParser(new ByteArrayDataSource(new ByteArrayInputStream(this.actual)));
        parser.parse();

        try (PreflightDocument document = parser.getPreflightDocument()) {
            document.validate();
            ValidationResult result = document.getResult();
            if (!result.isValid()) {
                final StringBuilder sb = new StringBuilder();
                sb.append("PDF is not PDF/A!");
                // for interpretation of error codes see:
                // https://github.com/apache/pdfbox/blob/trunk/preflight/src/main/java/org/apache/pdfbox/preflight/PreflightConstants.java
                for (ValidationResult.ValidationError error : result.getErrorsList()) {
                    sb.append(System.lineSeparator()).append(error.getErrorCode()).append(" : ").append(error.getDetails());
                }
                failWithMessage(sb.toString());
                return this;
            }
        }
        return this;
    }

    public PdfContentAssertion hasNumberOfPages(int expectedNumberOfPages) {

        isNotNull();
        if (actualNumberOfPages == null) {
            try (PDDocument document = PDDocument.load(this.actual)) {
                actualNumberOfPages = document.getNumberOfPages();
            } catch (IOException e) {
                e.printStackTrace();
                failWithMessage("Cannot parse PDF to check its number of pages!");
                return this;
            }
        }
        if (actualNumberOfPages != expectedNumberOfPages) {
            failWithMessage("Expected PDF to contain %d pages, but there are %d pages!",
                expectedNumberOfPages, actualNumberOfPages);
        }
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public PdfContentAssertion containsTextStringOnAnyPage(String text) {

        isNotNull();
        if (allText == null) {
            try {
                allText = readAndParseTextFromPdf();
            } catch (IOException e) {
                e.printStackTrace();
                failWithMessage("Cannot parse PDF to check, if it contains \"%s\"!", text);
                return this;
            }
        }
        if (!allText.contains(text)) {
            failWithMessage("Expected PDF to contain \"%s\", but there was none in:\r\n%s", text, allText);
        }
        return this;
    }

    /**
     * Tests, whether the given text is on a given page with a given area
     * @param page  page number
     * @param position page area - hint: the area of the complete paragraph is checked!
     * @param text the text to be contained
     * @return assertion object
     */
    public PdfContentAssertion containsTextStringAtPosition(int page, RectangleInMm position, String text) {

        isNotNull();
        if (locations == null) {
            try {
                locations = readAndParseTextLocationsFromPdf();
            } catch (IOException e) {
                e.printStackTrace();
                failWithMessage("Cannot parse PDF to check, if it contains \"%s\" within %s on page %d!",
                    text, position, page);
                return this;
            }
        }

        if (!containsAtPosition(locations, position, text)) {
            failWithMessage("Expected PDF to contain \"%s\" within %s on page %d, but there was none in: %s",
                text, position, page, dumpAllLocations());
        }
        return this;
    }

    //------------------------------------------------------------------------------------------------------------------

    private String readAndParseTextFromPdf() throws IOException {

        // From: https://www.tutorialspoint.com/pdfbox/pdfbox_reading_text.htm
        try (PDDocument document = PDDocument.load(this.actual)) {
            final PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    private List<TextLocation> readAndParseTextLocationsFromPdf() throws IOException {

        List<TextLocation> locations;
        try (PDDocument document = PDDocument.load(this.actual)) {
            PdfTextLocator pdfTextLocator = new PdfTextLocator(document, this.pointAdaption, this.scaleAdaption);
            locations = pdfTextLocator.retrieveLocations();
        }
        return locations;
    }

    private boolean containsAtPosition(List<TextLocation> locations, RectangleInMm position, String text) {

        for (TextLocation location : locations) {
            if (location.text().contains(text) && isWithIn(location, position)) {
                return true;
            }
        }
        return false;
    }

    private boolean isWithIn(TextLocation location, RectangleInMm position) {

        return location.x() > position.leftUpperX()
            && location.getRightLowerX() < position.rightLowerX()
            && location.y() > position.leftUpperX()
            && location.getRightLowerY() < position.rightLowerY();
    }

    private String dumpAllLocations() {

        StringBuilder ret = new StringBuilder();
        for (TextLocation location : locations) {
            ret.append("\r\n- \"").append(location.printPos()).append(" = \"").append(location.text()).append("\"");
        }
        return ret.toString();
    }
}
