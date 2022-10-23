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
import java.nio.file.Files;

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
    void givenDirTargetDirIsNotEmpty_whenCopyResourceFilesFlat_withoutCleanIfExists_thenTargetIsNotCleaned() throws IOException {
        final File testDirectory = File.createTempFile("test-","");
        // Delete temp file - we need a directory!
        assertThat(testDirectory.delete()).isTrue();
        assertThat(testDirectory.mkdir()).isTrue();

        try {
            final File testFile1 = File.createTempFile("test-",null, testDirectory);
            final File testFile2 = File.createTempFile("test-",null, testDirectory);
            int count = FileUtil.copyResourceFiles("testdata/file-util/subdir1", ".txt", testDirectory, false, true);
            assertThat(count).isEqualTo(2);
            assertThat(testDirectory.listFiles()).hasSize(4);
            assertThat(testFile1.exists()).isTrue();
            assertThat(testFile2.exists()).isTrue();
        } finally {
            FileUtils.deleteDirectory(testDirectory);
        }
    }

    @Test
    void givenDirTargetDirIsNotEmpty_whenCopyResourceFilesFlat_withCleanIfExists_thenTargetIsCleaned() throws IOException {
        final File testDirectory = File.createTempFile("test-","");
        // Delete temp file - we need a directory!
        assertThat(testDirectory.delete()).isTrue();

        try {
            assertThat(testDirectory.mkdir()).isTrue();
            final File testFile1 = File.createTempFile("test-",null, testDirectory);
            final File testFile2 = File.createTempFile("test-",null, testDirectory);
            int count = FileUtil.copyResourceFiles("testdata/file-util/subdir1", ".txt", testDirectory, true, true);
            assertThat(count).isEqualTo(2);
            assertThat(testDirectory.listFiles()).hasSize(2);
            assertThat(testFile1.exists()).isFalse();
            assertThat(testFile2.exists()).isFalse();
        } finally {
            FileUtils.deleteDirectory(testDirectory);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "1-flat,testdata/file-util/subdir1,.txt,true,2,2,file2.txt",
        "2-flat,testdata/file-util/subdir2,.txt,true,1,1,file1.txt",
        "3-flat,testdata/file-util/subdir1,.json,true,1,1,file1.json",
        "4-flat,testdata/file-util/,.txt,true,4,2,file2.txt",
        "5-flat,testdata/file-util/,,true,6,3,file1.json",

        "1-deep,testdata/file-util/subdir1,.txt,false,2,2,file2.txt",
        "2-deep,testdata/file-util/subdir2,.txt,false,1,1,file1.txt",
        "3-deep,testdata/file-util/subdir1,.json,false,1,1,file1.json",
        "4-deep,testdata/file-util/,.txt,false,4,4,subdir2/file1.txt",
        "5-deep,testdata/file-util/,,false,6,6,subdir2/file1.json",

        "6,testdata/file-util/subdir1/simple,.xyz,true,0,0,",
    })
    void givenDirExistsWithFiles_whenCopyResourceFilesFlat_thenReturnNumberOfCopies(
        String testId, String resourcePath, String endsWith, boolean flatten,
        int expectedCopies, int expectedUnique, String expectedExampleFilename) throws IOException {

        final File testDirectory = File.createTempFile("test-" + testId + "-","");
        // Delete temp file - we need a directory!
        assertThat(testDirectory.delete()).isTrue();
        try {
            int copied = FileUtil.copyResourceFiles(resourcePath, endsWith, testDirectory, true, flatten);
            assertThat(copied).isEqualTo(expectedCopies);
            if (copied > 0) {
                // We test only one example file for existence
                final File expectedFile = new File(testDirectory, expectedExampleFilename);
                assertThat(expectedFile.exists()).isTrue();
                assertThat(expectedFile.length()).isGreaterThan(0);
                // count files recursively int the target directory
                long fileCount = Files.find(testDirectory.toPath(), 4,
                        (filePath, fileAttr) -> fileAttr.isRegularFile()).count();
                assertThat(fileCount).isEqualTo(expectedUnique);
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