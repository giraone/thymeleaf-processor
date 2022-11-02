package com.giraone.thymeleaf.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "squid:S125", "squid:S1168"}) // Commented out code, Return empty collection
public final class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
    private static final String SRC_RESOURCES = "./src/main/resources";
    private static final String PD4ML_FONTS_PROPERTIES = "defaultfonts/pd4fonts.properties";

    private static final PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();

    // Hide
    private FileUtil() {
    }

    public static URL getUrlFromResource(String resourcePath) {
        final URL url;
        try {
            url = ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX + resourcePath);
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
                return ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + resourcePath);
            } catch (FileNotFoundException e) {
                LOGGER.error("Cannot check resource URL for \"{}\".", resourcePath, e);
                return null;
            }
        }

        // The difficult part. We need a file URL, but we are running from a JAR file, where there is no file system,
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
            url = ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX + resourcePath);
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

    public static List<String> getPd4mlFontNames() {
        try {
            var stream = readTextFileFromResource(PD4ML_FONTS_PROPERTIES);
            if (stream != null) {
                return stream
                    .lines()
                    .filter(e -> !e.startsWith("#"))
                    .map(e -> e.split("="))
                    .filter(e -> e.length == 2)
                    .map(e -> e[0].replace("\\ ", " "))
                    .collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        } catch (IOException e) {
            LOGGER.error("Cannot read PD4ML fonts from resources", e);
            return Collections.emptyList();
        }
    }

    //------------------------------------------------------------------------------------------------------------------

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

    static String extractNeededParentPath(String parentSourcePath, String resourceRoot) {

        StringBuilder sb = new StringBuilder();
        while (true) {
            if (parentSourcePath.endsWith(resourceRoot)) {
                return sb.toString();
            }
            int i = parentSourcePath.lastIndexOf('/');
            if (i < 1) {
                return null;
            }
            sb.insert(0, "/").insert(0, parentSourcePath.substring(i + 1));
            parentSourcePath = parentSourcePath.substring(0, i);
        }
    }
}
