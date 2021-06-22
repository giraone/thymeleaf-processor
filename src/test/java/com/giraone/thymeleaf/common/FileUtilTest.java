package com.giraone.thymeleaf.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"squid:S100", "squid:S1075"}) // naming, configurable path
class FileUtilTest {

    private static final String EXISTING_PATH = "testdata/input/simple";
    private static final String SUFFIX = ".html";
    private static final String NOT_EXISTING_PATH = "testdata/input/not-exists";
    private static final String EXISTING_FILE = "testdata/input/simple/input.html";
    private static final String NON_EXISTING_FILE = "testdata/input/not-exists.html";
    private static final String IMAGE = "images/test-image.png";
    private static final long IMAGE_BYTES = 187;

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

    @Test
    void givenDirExistsWithoutEndsWith_whenGetFilesInResourceFolder_thenReturnList() {

        List<File> result = FileUtil.getFilesInResourceFolder(EXISTING_PATH, null);
        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result.get(0).getAbsolutePath()).endsWith(".css");
    }

    @Test
    void givenDirExistsWithEndsWith_whenGetFilesInResourceFolder_thenReturnList() {

        List<File> result = FileUtil.getFilesInResourceFolder(EXISTING_PATH, SUFFIX);
        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result.get(0).getAbsolutePath()).endsWith(SUFFIX);
    }

    @Test
    void givenDirExistsButWrongEndsWith_whenGetFilesInResourceFolder_thenReturnEmptyArray() {

        List<File> result = FileUtil.getFilesInResourceFolder(EXISTING_PATH, ".xxx");
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    void givenDirExistsNot_whenGetFilesInResourceFolder_thenReturnNull() {

        List<File> result = FileUtil.getFilesInResourceFolder(NOT_EXISTING_PATH, SUFFIX);
        assertThat(result).isNull();
    }

    @Test
    void assertThat_readBytesFromUrl_works() throws IOException {

        URL url = ClassLoader.getSystemClassLoader().getResource(IMAGE);
        assertThat(url).isNotNull();
        byte[] result = FileUtil.readBytesFromUrl(url);
        assertThat(result).isNotNull();
        assertThat(result.length).isEqualTo(IMAGE_BYTES);
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