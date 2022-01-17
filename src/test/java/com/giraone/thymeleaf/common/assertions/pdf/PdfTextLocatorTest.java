package com.giraone.thymeleaf.common.assertions.pdf;

import com.giraone.thymeleaf.common.FileUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PdfTextLocatorTest {

    @ParameterizedTest
    @CsvSource({
        "pdfs/pdf-with-some-text-via-word-export.pdf",
        "pdfs/pdf-with-some-text-via-word-export-pdfa.pdf",
        "pdfs/pdf-with-some-text-via-pixel-planet-print.pdf",
        "pdfs/pdf-with-some-text-via-pixel-planet-print-pdfa.pdf"
    })
    void assertThat_retrieveLocations_worksBasically(String resourcePath) throws IOException {

        // arrange
        byte[] pdfBytes = FileUtil.readBytesFromResource(resourcePath);

        // act
        PointAdaption pointAdaption = new PointAdaption(0.0f,0.0f);
        ScaleAdaption scaleAdaption = new ScaleAdaption(0.0f,0.0f);
        List<TextLocation> result;
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PdfTextLocator pdfTextLocator = new PdfTextLocator(document, pointAdaption, scaleAdaption);
            result = pdfTextLocator.retrieveLocations();
        }

        // assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(5);

        System.err.println(result);
        TextLocation title = result.get(0);
        assertThat(title.text()).contains("Title of Document");
        assertThat(title.page()).isEqualTo(1);
        /*
        assertThat(title.x()).isGreaterThan(60.0f).isLessThan(80.0f);
        assertThat(title.y()).isGreaterThan(80.0f).isLessThan(100.0f); // first line
        assertThat(title.width()).isGreaterThan(70.0f).isLessThan(100.0f);
        assertThat(title.height()).isGreaterThan(9.0f).isLessThan(10.0f);  // large font
        */

        TextLocation header1 = result.get(1);
        assertThat(header1.text()).contains("Header for Section 1");

        TextLocation text1 = result.get(2);
        assertThat(text1.text()).contains("Some monospaced text in 1");

        TextLocation header2 = result.get(3);
        assertThat(header2.text()).contains("Header for Section 2");

        TextLocation text2 = result.get(4);
        assertThat(text2.text()).contains("More text in section 2 in the middle.");
    }
}