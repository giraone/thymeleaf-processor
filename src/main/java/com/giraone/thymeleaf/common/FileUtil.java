package com.giraone.thymeleaf.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"unused", "squid:S125", "squid:S1168"}) // Commented out code, Return empty collection
public final class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String SRC_RESOURCES = "./src/main/resources";

    // Hide
    private FileUtil() {
    }

    private static ClassLoader getClassLoader() {

        // Resources are in BOOT-INF - this should work.
        return FileUtil.class.getClassLoader();
        // If not, see https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-executable-jar-format.html#executable-jar-restrictions
        // return Thread.currentThread().getContextClassLoader();
    }

    public static File getFileFromResource(String resourcePath) {

        final URL url;
        try {
            url = ResourceUtils.getURL(CLASSPATH_PREFIX + resourcePath);
        } catch (FileNotFoundException e) {
            LOGGER.error("Cannot read resource \"{}\". Path not found!", resourcePath, e);
            return null;
        }

        if (ResourceUtils.isFileURL(url)) { // The easy part. We are running from a classes folder.
            try {
                return ResourceUtils.getFile(CLASSPATH_PREFIX + resourcePath);
            } catch (FileNotFoundException e) {
                LOGGER.error("Cannot check resource URL for \"{}\".", resourcePath, e);
                return null;
            }
        }

        // The difficult part. We need a file URL, but we are running from a JAR file, where there ist no file system,
        // There are only zipped archives. So we copy the content to a temporary file.
        final String[] prefixAndSuffix = extractPrefixAndSuffix(url.getPath());
        final String prefix = prefixAndSuffix[0];
        final String suffix = prefixAndSuffix[1];
        File tempFile;
        try {
            tempFile = File.createTempFile(prefix, suffix);
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                try (InputStream inputStream = url.openStream()) {
                    IoStreamUtil.pipeBlobStream(inputStream, outputStream);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Cannot create temp file to copy resource \"{}\" from JAR file.", resourcePath, e);
            return null;
        }
        return tempFile;
    }

    public static byte[] readBytesFromResource(String resourcePath) throws IOException {

        final File file;
        try {
            file = ResourceUtils.getFile(CLASSPATH_PREFIX + resourcePath);
        } catch (FileNotFoundException e) {
            LOGGER.error("Cannot read resource \"{}\"", resourcePath, e);
            return null;
        }
        try (InputStream in = new FileInputStream(file)) {
            return in.readAllBytes();
        }
    }

    public static String readTextFileFromResource(String resourcePath) throws IOException {
        return readTextFileFromResource(resourcePath, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unused")
    public static String readTextFileFromResource(String resourcePath, Charset encoding) throws IOException {

        byte[] bytes = readBytesFromResource(resourcePath);
        return bytes == null ? null : new String(bytes, encoding);
    }

    public static List<File> getFilesInResourceFolderOrSrcDir(String path, FilenameFilter filter) {

        final File srcFolder = new File(SRC_RESOURCES);
        if (!srcFolder.exists()) {
            return getFilesInResourceFolder(path, filter);
        }
        final File[] fileList = new File(srcFolder + path).listFiles(filter);
        if (fileList == null) {
            return null;
        }
        return new ArrayList<>(Arrays.asList(fileList));
    }

    public static List<File> getFilesInResourceFolder(String path, FilenameFilter filter) {

        final URL url = getClassLoader().getResource(path);
        if (url == null) {
            return null;
        } else {
            File dir;
            try {
                dir = new File(url.toURI());
            } catch (URISyntaxException e) {
                LOGGER.debug("invalid url: {}", url);
                return null;
            }
            final File[] fileList = dir.listFiles(filter);
            if (fileList == null) {
                return null;
            }
            return new ArrayList<>(Arrays.asList(fileList));
        }
    }

    public static boolean fileExistsInResourceOrSrcDir(String resourcePath) {

        final File srcFolder = new File(SRC_RESOURCES);
        if (srcFolder.exists()) {
            return new File(srcFolder.getAbsolutePath() + resourcePath).exists();
        } else {
            return fileExistsInResource(resourcePath);
        }
    }

    public static boolean fileExistsInResource(String resourcePath) {

        final URL url = getClassLoader().getResource(resourcePath);
        if (url == null) {
            return false;
        }
        final File file = new File(url.getFile());
        return file.exists();
    }

    public static byte[] readBytesFromUrl(URL url) throws IOException {

        if (url == null) {
            throw new IllegalArgumentException("URL cannot be null!");
        }
        if ("classpath".equals(url.getProtocol())) {
            return readBytesFromResource(url.getPath());
        }
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = url.openStream()) {
            IoStreamUtil.pipeBlobStream(inputStream, outputStream);
            return outputStream.toByteArray();
        }
    }

    static String[] extractPrefixAndSuffix(String path) {

        int i = path.lastIndexOf('/');
        if (i >= 0) {
            path = path.substring(i + 1);
        }
        i = path.lastIndexOf(".");
        String prefix;
        String suffix;
        if (i >= 0) {
            prefix = path.substring(0, i);
            suffix = path.substring(i);
        } else {
            prefix = path;
            suffix = "";
        }
        return new String[] { prefix, suffix };
    }
}
