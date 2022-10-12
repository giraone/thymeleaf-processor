package com.giraone.thymeleaf.service.convert;

import com.giraone.thymeleaf.common.FileUtil;
import com.giraone.thymeleaf.config.ApplicationProperties;
import com.pd4ml.Constants;
import com.pd4ml.PD4ML;
import com.pd4ml.StatusMessage;
import io.micrometer.core.annotation.Timed;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zefer.pd4ml.PD4Constants;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Service
@SuppressWarnings("squid:S3008") // Naming
public class HtmlToPdfConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlToPdfConverter.class);

    private static final String PD4ML_LICENSE_RESOURCE_PATH = "pd4ml/pd4ml.lic";
    private static final String PD4ML_FONTS_RESOURCE_PATH = "defaultfonts";
    private static final String PD4ML_LICENSE_PATH;
    private static final String PD4ML_FONTS_TEMP_DIRECTORY_NAME_PREFIX = "pd4ml-fonts-";
    private static final int PD4ML_FIXED_HTML_WIDTH = 842;

    static {
        final File pd4mlLicFile = FileUtil.getFileFromResource(PD4ML_LICENSE_RESOURCE_PATH);
        if (pd4mlLicFile == null) {
            // Helpful in some local dev situations
            String licFileInFileSystem = "./src/main/resources/" + PD4ML_LICENSE_RESOURCE_PATH;
            if (new File(licFileInFileSystem).exists()) {
                PD4ML_LICENSE_PATH = licFileInFileSystem;
                LOGGER.error("PD4ML license not found in resource \"{}\"! Switch to relative src/main development location \"{}\"",
                    PD4ML_LICENSE_RESOURCE_PATH, PD4ML_LICENSE_PATH);
            } else {
                PD4ML_LICENSE_PATH = null;
                LOGGER.warn("PD4ML license neither found in resource \"{}\" or in file system path \"{}\"",
                    PD4ML_LICENSE_RESOURCE_PATH, licFileInFileSystem);
            }
        } else {
            PD4ML_LICENSE_PATH = pd4mlLicFile.getPath();
            LOGGER.info("Using PD4ML license \"{}\"", PD4ML_LICENSE_PATH);
        }

        final File fontsDirectory = getFontsDirectory();
        final int count = FileUtil.copyResourceFiles(PD4ML_FONTS_RESOURCE_PATH, "ttf", fontsDirectory, true);
        if (count > 0) {
            LOGGER.info("{} TTF font files copied to \"{}\"", count, fontsDirectory);
        } else {
            LOGGER.warn("No TTF font files were copied from \"{}\" to \"{}\"", PD4ML_FONTS_RESOURCE_PATH, fontsDirectory);
        }
    }

    private final ApplicationProperties applicationProperties;

    @Autowired
    public HtmlToPdfConverter(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Timed
    public void generatePdfStreamFromHtmlStream(InputStream htmlInputStream, URL url, OutputStream pdfOutputStream,
                                                PdfCreationOptions pdfCreationOptions) throws IOException {

        final int logLevel = applicationProperties.getPd4mlConfiguration().getLogLevel();
        final PD4ML pd4ml = new PD4ML(PD4ML_LICENSE_PATH, logLevel);
        configureOptions(pd4ml, pdfCreationOptions);
        pd4ml.readHTML(htmlInputStream, url);
        pd4ml.setHtmlWidth(PD4ML_FIXED_HTML_WIDTH);
        if (!StringUtils.isEmpty(pdfCreationOptions.getDocumentTitle())) {
            pd4ml.setDocumentTitle(pdfCreationOptions.getDocumentTitle());
        }
        if (!StringUtils.isEmpty(pdfCreationOptions.getDocumentAuthor())) {
            pd4ml.setAuthorName(pdfCreationOptions.getDocumentAuthor());
        }
        if (pdfCreationOptions.isPdfA()) {
            pd4ml.writePDF(pdfOutputStream, Constants.PDFA);
            if (LOGGER.isInfoEnabled()) {
                logPdfAStatusMessages(pd4ml);
            }
        } else {
            pd4ml.writePDF(pdfOutputStream);
        }
    }

    public void generatePdfStreamFromHtmlString(String htmlString, OutputStream pdfOutputStream, PdfCreationOptions pdfCreationOptions)
        throws IOException {

        try (InputStream htmlInputStream = new ByteArrayInputStream(htmlString.getBytes())) {
            generatePdfStreamFromHtmlStream(htmlInputStream, null, pdfOutputStream, pdfCreationOptions);
        }
    }

    public void generatePdfStreamFromHtmlUrl(URL inputHtmlUrl, OutputStream pdfOutputStream, PdfCreationOptions pdfCreationOptions)
        throws IOException {

        try (InputStream htmlInputStream = inputHtmlUrl.openStream()) {
            generatePdfStreamFromHtmlStream(htmlInputStream, inputHtmlUrl, pdfOutputStream, pdfCreationOptions);
        }
    }

    public void generatePdfFileFromHtmlFile(File inputHtmlFile, File outputPdfFile, PdfCreationOptions pdfCreationOptions)
        throws IOException {

        try (
            final FileOutputStream pdfOutputStream = new FileOutputStream(outputPdfFile);
            InputStream htmlInputStream = new FileInputStream(inputHtmlFile)
        ) {
            generatePdfStreamFromHtmlStream(htmlInputStream, inputHtmlFile.toURI().toURL(), pdfOutputStream, pdfCreationOptions);
        }
    }

    private void configureOptions(PD4ML pd4ml, PdfCreationOptions pdfCreationOptions) throws FileNotFoundException {

        pd4ml.setDocumentTitle(pdfCreationOptions.getDocumentTitle());
        pd4ml.setAuthorName(pdfCreationOptions.getDocumentAuthor());
        if (pdfCreationOptions.getWatermarkOptions() != null) {
            final PdfCreationOptions.WatermarkOptions options = pdfCreationOptions.getWatermarkOptions();
            final String htmlCode = StringEscapeUtils.escapeHtml4(options.getText());
            pd4ml.setWatermark(
                htmlCode,
                options.getLeft(), options.getTop(), options.getOpacity(), options.getAngle(), options.getScale(),
                true, true, "1"); // screen, print, scope
        } else {
            pd4ml.setWatermark("", 0, 0, 0, 0, 0, false, false, "");
        }

        // Load and embed fonts. See: https://pd4ml.tech/pdf-fonts/
        // Font embedding is a pre-requisite to PDF/A, but is also necessary for non PDF/A, when custom fonts are used.
        String fontDir = System.getenv("PD4ML_FONTS");
        if (fontDir != null) {
            LOGGER.info("Using fonts from environment variable \"{}\"", fontDir);
        } else {
            fontDir = getFontsDirectory().getAbsolutePath();
            LOGGER.info("Using fonts from temp directory \"{}\" with copied resources", fontDir);
        }
        pd4ml.useTTF(fontDir, true);

        Map<String, String> dynamicParams = new HashMap<>();
        // Add Support for media type "print" in addition to all, screen, pdf
        dynamicParams.put(PD4Constants.PD4ML_MEDIA_TYPE_PRINT, "add");
        pd4ml.setDynamicData(dynamicParams);
    }

    private void logPdfAStatusMessages(PD4ML pd4ml) throws com.pd4ml.ForcedTerminationException {

        StatusMessage[] messages = (StatusMessage[]) pd4ml.getLastRenderInfo(Constants.PD4ML_PDFA_STATUS);
        for (StatusMessage statusMessage : messages) {
            String message = (statusMessage.isError() ? "ERROR: " : "WARNING: ") + statusMessage.getMessage();
            LOGGER.info(message);
        }
    }

    private static File getFontsDirectory() {
        final long pid = ProcessHandle.current().pid();
        return new File(System.getProperty("java.io.tmpdir"), PD4ML_FONTS_TEMP_DIRECTORY_NAME_PREFIX + pid);
    }
}