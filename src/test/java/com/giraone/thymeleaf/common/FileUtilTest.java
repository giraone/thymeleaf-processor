package com.giraone.thymeleaf.common;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"squid:S100", "squid:S1075"}) // naming, configurable path
class FileUtilTest {

    private static final String EXISTING_FILE = "testdata/input/simple/input.html";
    private static final String NON_EXISTING_FILE = "testdata/input/not-exists.html";
    private static final String IMAGE = "images/test-image.png";
    private static final long IMAGE_BYTES = 187;
    private static final String FONT_NAME_THAT_IS_IN_RESOURCES = "Roboto";

    @Test
    void givenResourceExists_whenGetTextFileFromResource_thenReturnFile() {
        File textFileFromResource = FileUtil.getFileFromResource(EXISTING_FILE);
        assertThat(textFileFromResource).isNotNull();
    }

    @Test
    void givenResourceExistsNot_whenGetTextFileFromResource_thenReturnNull() {
        File textFileFromResource = FileUtil.getFileFromResource(NON_EXISTING_FILE);
        assertThat(textFileFromResource).isNull();
    }

    @Test
    void givenTextFileExists_whenReadTextFileFromResource_thenContentIsRead() throws IOException {
        String contentAsString = FileUtil.readTextFileFromResource(EXISTING_FILE, StandardCharsets.UTF_8);
        assertThat(contentAsString).isNotNull();
    }

    @Test
    void givenTextFileExistsNot_whenReadTextFileFromResource_thenReturnNull() throws IOException {
        String contentAsString = FileUtil.readTextFileFromResource(NON_EXISTING_FILE, StandardCharsets.UTF_8);
        assertThat(contentAsString).isNull();
    }

    @Test
    void givenFileExists_whenFileExistsInResource_thenReturnTrue() {
        boolean fileExists = FileUtil.fileExistsInResource(EXISTING_FILE);
        assertThat(fileExists).isTrue();
    }

    @Test
    void givenFileExistsNot_whenFileExistsInResource_thenReturnFalse() {
        boolean fileExists = FileUtil.fileExistsInResource(NON_EXISTING_FILE);
        assertThat(fileExists).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
        "testdata/input/simple,.html,2,input.html",
        "testdata/input/simple,.json,1,input.json",
        "testdata/input/simple,.xyz,0,",
        "testdata/input/**,.css,4,input.css",
        "testdata/input/simple,,4,input.css",
    })
    void givenDirExistsWithFiles_whenCopyResourceFiles_thenReturnNumberOfCopies(
        String resourcePath, String endsWith, int expectedCopies, String expectedFilename) throws IOException {

        final long pid = ProcessHandle.current().pid();
        final File testDirectory = new File(System.getProperty("java.io.tmpdir"), "test-" + pid);
        try {
            int count = FileUtil.copyResourceFiles(resourcePath, endsWith, testDirectory, true);
            assertThat(count).isEqualTo(expectedCopies);
            if (count > 0) {
                final File expectedFile = new File(testDirectory, expectedFilename);
                assertThat(expectedFile.exists()).isTrue();
                assertThat(expectedFile.length()).isGreaterThan(0);
            }
        } finally {
            FileUtils.deleteDirectory(testDirectory);
        }
    }

    @Test
    void assertThat_readBytesFromUrl_works() throws IOException {

        URL url = ClassLoader.getSystemClassLoader().getResource(IMAGE);
        assertThat(url).isNotNull();
        byte[] result = FileUtil.readBytesFromUrl(url);
        assertThat(result).isNotNull();
        assertThat(result.length).isEqualTo(IMAGE_BYTES);
    }

    @Test
    void assertThat_getFontNames_works() {
        assertThat(FileUtil.getPd4mlFontNames()).contains(FONT_NAME_THAT_IS_IN_RESOURCES);
        assertThat(FileUtil.getPd4mlFontNames()).isNotEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "path/file.txt,file,.txt",
        "path/file,file,",
        "path/path/file.txt,file,.txt",
        "/file.txt,file,.txt"
    })
    void extractPrefixAndSuffix(String input, String expectedPrefix, String expectedSuffix) {

        // act
        String[] prefixAndSuffix = FileUtil.extractPrefixAndSuffix(input);

        // assert
        Assertions.assertAll(
            () -> assertThat(prefixAndSuffix[0]).isEqualTo(expectedPrefix),
            () -> assertThat(prefixAndSuffix[1]).isEqualTo(expectedSuffix == null ? "" : expectedSuffix)
        );
    }
}