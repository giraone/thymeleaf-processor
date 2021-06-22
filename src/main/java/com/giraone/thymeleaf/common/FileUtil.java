package com.giraone.thymeleaf.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private static final PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();

    // Hide
    private FileUtil() {
    }

    public static URL getUrlFromResource(String resourcePath) {
        final URL url;
        try {
            url = ResourceUtils.getURL(CLASSPATH_PREFIX + resourcePath);
        } catch (FileNotFoundException e) {
            LOGGER.error("Cannot read resource \"{}\". Path not found!", resourcePath, e);
            return null;
        }
        return url;
    }

    public static File getFileFromResource(String resourcePath) {

        final URL url = getUrlFromResource(resourcePath);
        if (url == null) {
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

        final URL url;
        try {
            url = ResourceUtils.getURL(CLASSPATH_PREFIX + resourcePath);
        } catch (FileNotFoundException e) {
            LOGGER.error("Cannot read resource \"{}\". Path not found!", resourcePath, e);
            return null;
        }
        try (InputStream in = url.openStream()) {
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

    @SuppressWarnings("squid:S1075") // Hard coded file delimiter
    public static List<File> getFilesInResourceFolderOrSrcDir(String path, String endsWith) {

        final File srcFolder = new File(SRC_RESOURCES);
        if (!srcFolder.exists()) {
            return getFilesInResourceFolder(path, endsWith);
        }
        final File[] fileList = new File(srcFolder + "/" + path)
            .listFiles((dir, name) -> endsWith == null || name.endsWith(endsWith));
        if (fileList == null) {
            return null;
        }
        return new ArrayList<>(Arrays.asList(fileList));
    }

    @SuppressWarnings("squid:S1075") // Hard coded file delimiter
    public static List<File> getFilesInResourceFolder(String path, String endsWith) {

        final String matchingPath = endsWith != null
            ? CLASSPATH_PREFIX + path + "/*" + endsWith
            : CLASSPATH_PREFIX + path + "/*";
        Resource[] resources;
        try {
            resources = pathMatchingResourcePatternResolver.getResources(matchingPath);
        } catch (IOException e) {
            LOGGER.warn("Cannot list resources: {}/{}", path, endsWith);
            return null;
        }
        final List<File> ret = new ArrayList<>();
        for (Resource resource: resources) {
            try {
                final File file = resource.getFile();
                ret.add(file);
            } catch (IOException e) {
                LOGGER.warn("Cannot build file from resource {}", resource.getFilename());
            }
        }
        return ret;
    }

    @SuppressWarnings("squid:S1075") // Hard coded file delimiter
    public static boolean fileExistsInResourceOrSrcDir(String resourcePath) {

        final File srcFolder = new File(SRC_RESOURCES);
        if (srcFolder.exists()) {
            return new File(srcFolder.getAbsolutePath() + "/" + resourcePath).exists();
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

    //------------------------------------------------------------------------------------------------------------------

    static ClassLoader getClassLoader() {

        // See https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-executable-jar-format.html#executable-jar-restrictions
        // return Thread.currentThread().getContextClassLoader();

        // Geht nicht (hs)
        // return ClassLoader.getSystemClassLoader();

        // Ressourcen sind in BOOT-INF - das sollte gehen (hs)
        return FileUtil.class.getClassLoader();
    }

    static String[] extractPrefixAndSuffix(String path) {

        int i = path.lastIndexOf('/');
        if (i >= 0) {
            path = path.substring(i + 1);
        }
        i = path.lastIndexOf('.');
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
