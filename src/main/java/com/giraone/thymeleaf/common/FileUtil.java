package com.giraone.thymeleaf.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public final class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    private static final String SRC_RESOURCES = "./src/main/resources";

    // Hide
    private FileUtil() {
    }

    public static File getFileFromResource(String resourcePath) {

        final URL url = FileUtil.class.getResource(resourcePath);
        if (url == null) {
            return null;
        }
        final File file = new File(url.getFile());
        if (!file.exists()) {
            return null;
        }
        return file;
    }

    public static byte[] readBytesFromResource(String resourcePath) throws IOException {

        final File file = getFileFromResource(resourcePath);
        return file == null ? null : Files.readAllBytes(file.toPath());
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

        File srcFolder = new File(SRC_RESOURCES);
        if (!srcFolder.exists()) {
            return getFilesInResourceFolder(path, filter);
        }
        File[] fileList = new File(srcFolder + path).listFiles(filter);
        if (fileList == null) {
            return null;
        }
        return new ArrayList<>(Arrays.asList(fileList));
    }

    public static List<File> getFilesInResourceFolder(String path, FilenameFilter filter) {

        URL url = FileUtil.class.getResource(path);
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
            File[] fileList = dir.listFiles(filter);
            if (fileList == null) {
                return null;
            }
            return new ArrayList<>(Arrays.asList(fileList));
        }
    }

    public static boolean fileExistsInResourceOrSrcDir(String resourcePath) {

        File srcFolder = new File(SRC_RESOURCES);
        if (srcFolder.exists()) {
            return new File(srcFolder.getAbsolutePath() + resourcePath).exists();
        } else {
            return fileExistsInResource(resourcePath);
        }
    }

    public static boolean fileExistsInResource(String resourcePath) {

        final URL url = FileUtil.class.getResource(resourcePath);
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
}
