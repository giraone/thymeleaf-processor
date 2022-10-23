package com.giraone.thymeleaf.common;

import org.apache.commons.io.FileUtils;
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
import java.io.OutputStream;
import java.net.URISyntaxException;
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

    /**
     * Copy files from a resource path (classpath with file extension filter) to a target directory in the (local) file system.
     * The copied files will keep the last modification date of the original file.
     * @param resourcePath  A classpath: resource, e.g. "defaultfonts", "testdata/simple" or "testdata/". If the value ends with "/",
     * the directory is looked up recursively.
     * @param endsWith  An optional (maybe null) file extension, e.g. \".ttf\", \".css\", to be used as a filter.
     * @param targetDirectory The target directory, where the files are copied to. If the target directory does not exist,
     * it is automatically created.
     * @param cleanIfExists A flag to clean the directory, if it already exists.
     * @param flatten If true, the tree structure is ignored and all file are copied directly into the target directory.
     * @return the number of copied files.
     */
    public static int copyResourceFiles(String resourcePath, String endsWith, File targetDirectory, boolean cleanIfExists, boolean flatten) {

        String classPath;
        if (resourcePath.endsWith("/")) {
            classPath = resourcePath + "**/*";
            resourcePath = resourcePath.substring(0, resourcePath.length() - 1);
        } else {
            classPath = resourcePath + "/*";
        }
        if (endsWith != null) {
            classPath += endsWith;
        }
        final String matchingPath = ResourceUtils.CLASSPATH_URL_PREFIX + classPath;
        return copyMatchingResourceFiles(matchingPath, resourcePath, targetDirectory, cleanIfExists, flatten);
    }

    /**
     * Copy files from a resource path to a target directory in the (local) file system.
     * The copied files will keep the last modification date of the original file.
     * @param matchingPath A resource path resolved using {@link PathMatchingResourcePatternResolver}.
     * @param resourceRoot The root directory of the source - Needed, if the files are deep - and not flat - copied.
     * @param targetDirectory The target directory, where the files are copied to. If the target directory does not exist,
     * it is automatically created.
     * @param cleanIfExists A flag to clean the directory, if it already exists.
     * @param flatten If true, the tree structure is ignored and all file are copied directly into the target directory.
     * @return the number of copied files.
     */
    public static int copyMatchingResourceFiles(String matchingPath, String resourceRoot, File targetDirectory, boolean cleanIfExists, boolean flatten) {

        if (!targetDirectory.exists()) {
            if (!targetDirectory.mkdirs()) {
                throw new RuntimeException("Cannot create target directory \"" + targetDirectory + "\"!");
            }
        } else {
            if (cleanIfExists) {
                try {
                    FileUtils.cleanDirectory(targetDirectory);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot clean target directory \"" + targetDirectory + "\"!", e);
                }
            }
        }

        int count = 0;
        final Resource[] resources;
        try {
            resources = pathMatchingResourcePatternResolver.getResources(matchingPath);
        } catch (IOException e) {
            if (e.getMessage().contains("does not exist")) {
                // Silently ignore, when there are no matching files
                LOGGER.info("No files found in \"{}\"", matchingPath);
                return 0;
            }
            throw new RuntimeException("Cannot lookup files in \"" + matchingPath + "\"!", e);
        }
        for (Resource resource : resources) {
            // Directories are not readable
            if (!resource.isReadable()) {
                LOGGER.debug("Skipping unreadable resource \"{}\"", resource);
                continue;
            }
            final String filename = resource.getFilename();
            if (filename == null) {
                continue;
            }
            try {
                final URL url;
                try {
                    url = resource.getURL();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Cannot get URL for resource " + resource
                        + " file=\"" + resource.getFilename() + "\"!", e);
                }

                final File targetFile;
                if (flatten) {
                    targetFile = new File(targetDirectory, filename);
                } else {
                    final File resourceFile;
                    try {
                        resourceFile = new File(url.toURI().getSchemeSpecificPart());
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                    final String parentPath = resourceFile.getParent().replace('\\', '/');
                    final String targetParent = extractNeededParentPath(parentPath, resourceRoot);
                    if (targetParent == null) {
                        throw new RuntimeException("parentPath/resourceRoot mismatch parentPath=\"" + parentPath
                            + "\", resourceRoot=\"" + resourceRoot + "\"!");
                    }
                    targetFile = new File(targetDirectory, targetParent + filename);
                    final File targetParentFile = targetFile.getParentFile();
                    // Automatically create subdirectories, when flatten=false
                    if (!targetParentFile.exists()) {
                        boolean autoCreateTargetDirectories = targetParentFile.mkdirs();
                        if (!autoCreateTargetDirectories) {
                            throw new RuntimeException("Cannot create target directory \"" + targetParentFile + "\"!");
                        }
                    }
                }

                copyUrlContentToFile(url, targetFile);
                setLastModified(targetFile, resource.lastModified());
            } catch (IOException e) {
                throw new RuntimeException("Cannot copy resource file \"" + filename + "\" to \"" + targetDirectory + "\"!", e);
            }
            count++;
        }
        return count;
    }

    public static boolean fileExistsInResourceOrSrcDir(String resourcePath) {

        final File srcFolder = new File(SRC_RESOURCES);
        if (srcFolder.exists()) {
            return new File(srcFolder.getAbsolutePath(), resourcePath).exists();
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

    static ClassLoader getClassLoader() {

        // See https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-executable-jar-format.html#executable-jar-restrictions
        // return Thread.currentThread().getContextClassLoader();

        // Geht nicht in Tests, die z.B. von Kommandozeile mit mvn ohne fork laufen. Ist generell keine gute Idee. (hs)
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

    static void copyUrlContentToFile(URL url, File file) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            try (InputStream in = url.openStream()) {
                long copied = IoStreamUtil.pipeBlobStream(in, out);
                LOGGER.debug("Copied URL \"{}\" with {} bytes to file \"{}\"", url, copied, file);
            }
        }
    }

    static void setLastModified(File file, long lastModified) {

        boolean setLastModifiedOk = file.setLastModified(lastModified);
        if (!setLastModifiedOk) {
            LOGGER.warn("Cannot set modification date of file \"{}\"!", file);
        }
    }
}
