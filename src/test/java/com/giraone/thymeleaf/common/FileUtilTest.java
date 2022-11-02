package com.giraone.thymeleaf.common;

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

    private static final String EXISTING_FILE = "testdata/file-util/file1.txt";
    private static final String NON_EXISTING_FILE = "testdata/file-util/not-exists.txt";
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

    @ParameterizedTest
    @CsvSource(value = {
        "C:/test-classes/testdata/file-util/subdir1,testdata/file-util/subdir1,''",
        "C:/test-classes/testdata/file-util/subdir1,testdata/file-util,subdir1/",
        "C:/test-classes/testdata/file-util/subdir1,testdata,file-util/subdir1/",
        "C:/test-classes/testdata/file-util/subdir1,xxx,",
    })
    void extractNeededParentPath(String parentSourcePath, String resourceRoot, String expected) {

        String actual = FileUtil.extractNeededParentPath(parentSourcePath, resourceRoot);
        assertThat(actual).isEqualTo(expected);
    }
}