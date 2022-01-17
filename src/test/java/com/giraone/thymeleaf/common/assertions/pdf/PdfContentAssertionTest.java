package com.giraone.thymeleaf.common.assertions.pdf;

import com.giraone.thymeleaf.common.FileUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class PdfContentAssertionTest {

    @Test
    void assertThat_isValidPdf_basicallyWorks() throws IOException {

        // arrange
        byte[] pdfBytes = readTestPdfWithOnePage();

        // assert
        PdfContentAssertion.assertThat(pdfBytes).isValidPdf();
    }

    @Test
    void assertThat_isValidPdf_detectsHtmlPageAsInvalidPdf() {

        // assert
        PdfContentAssertion.assertThat(null).isNotValidPdf();
        PdfContentAssertion.assertThat("".getBytes()).isNotValidPdf();
        PdfContentAssertion.assertThat("%PDF-1.5".getBytes()).isNotValidPdf();
        PdfContentAssertion.assertThat(
            "<html><body>Some large text 0123456789 0123456789 0123456789 0123456789 0123456789</body></html>"
                .getBytes()).isNotValidPdf();
    }

    @Test
    void assertThat_containsTextStringAtPosition_basicallyWorks() throws IOException {

        // arrange
        byte[] pdfBytes = readTestPdfWithOnePage();

        // assert
        PdfContentAssertion.assertThat(pdfBytes)
            .isValidPdf()
            .containsTextStringAtPosition(1, new RectangleInMm(20, 30, 120, 40), "Title of Document")
            .containsTextStringAtPosition(1, new RectangleInMm(20, 45, 100, 50), "Header for Section 1")
            .containsTextStringAtPosition(1, new RectangleInMm(20, 55, 100, 60), "Some monospaced text in 1.")
            .containsTextStringAtPosition(1, new RectangleInMm(20, 65, 100, 75), "Header for Section 2")
            //.containsTextStringAtPosition(1, new RectangleInMm(95, 75, 180, 85), "More text in section 2 in the middle.")
        ;
    }

    @Test
    void assertThat_containsTextStringOnAnyPage_worksForSinglePagePdf() throws IOException {

        // arrange
        byte[] pdfBytes = readTestPdfWithOnePage();

        // assert
        PdfContentAssertion.assertThat(pdfBytes).isValidPdf();
        PdfContentAssertion.assertThat(pdfBytes).containsTextStringOnAnyPage("Header for Section");
    }

    @Test
    void assertThat_containsTextStringOnAnyPage_worksForMultiPagePdf() throws IOException {

        // arrange
        byte[] pdfBytes = readTestPdfWithTwoPages();

        // assert
        PdfContentAssertion.assertThat(pdfBytes).isValidPdf();
        PdfContentAssertion.assertThat(pdfBytes).containsTextStringOnAnyPage("Header for Section");
    }

    @Test
    void assertThat_containsTextStringAtPosition_worksForMultiPagePdf() throws IOException {

        // arrange
        byte[] pdfBytes = readTestPdfWithTwoPages();

        // assert
        PdfContentAssertion.assertThat(pdfBytes)
            .isValidPdf()
            .containsTextStringAtPosition(1, new RectangleInMm(20, 30, 120, 40), "Title of Document")
            .containsTextStringAtPosition(2, new RectangleInMm(20, 35, 100, 45), "Header for Section 3")
            //.containsTextStringAtPosition(2, new RectangleInMm(110, 55, 190, 65), "More text somewhere on the second page.")
            ;
    }

    //------------------------------------------------------------------------------------------------------------------

    private byte[] readTestPdfWithOnePage() throws IOException {
        String resourcePath = "pdfs/pdf-with-some-text.pdf";
        return FileUtil.readBytesFromResource(resourcePath);
    }

    private byte[] readTestPdfWithTwoPages() throws IOException {
        String resourcePath = "pdfs/pdf-with-some-text-two-pages.pdf";
        return FileUtil.readBytesFromResource(resourcePath);
    }
}