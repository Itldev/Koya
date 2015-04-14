package fr.itldev.koya.utils;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.ZipError;

import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.log4j.Logger;
import org.mozilla.universalchardet.UniversalDetector;

public class Zips {

    static Logger logger = Logger.getLogger(Zips.class);

    /**
     * Unzips the specified zip file to the specified destination directory.
     * Replaces any files in the destination, if they already exist.
     *
     * @param zipPath the name of the zip file to extract
     * @param destPath the directory to unzip to
     */
    public static boolean unzip(String zipPath, String destPath,
            String defaultCharset, final String failoverCharset) {
        try {
            final Path destDir = Paths.get(destPath);
            // if the destination doesn't exist, create it
            if (Files.notExists(destDir)) {
                logger.trace(destDir + " does not exist. Creating...");
                Files.createDirectories(destDir);
            }

            /* Define ZIP File System Properies in HashMap */
            Map<String, String> zipProperties = new HashMap<>();
            /* We want to read an existing ZIP File, so we set this to False */
            zipProperties.put("create", "false");
            String charset = determineCharset(zipPath);
            if (charset != null
                    && charset.toLowerCase().equals("windows-1252")) {
                // ibm850 (winzip?), is detected as windows-1252)
                charset = "ibm850";
            } else {
                charset = defaultCharset;
            }
            final String finalCharset = charset;

            zipProperties.put("encoding", finalCharset);
            logger.debug(zipPath + " will be extracted using "
                    + zipProperties.get("encoding"));

            // convert the filename to a URI
            final Path path = Paths.get(zipPath);
            final URI uri = URI.create("jar:file:" + path.toUri().getPath());

            try (FileSystem zipFileSystem = FileSystems.newFileSystem(uri,
                    zipProperties)) {
                final Path root = zipFileSystem.getPath("/");
                // walk the zip file tree and copy files to the destination
                Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file,
                            BasicFileAttributes attrs) throws IOException {
                        String filename = null;
                        try {
                            filename = file.toString();
                        } catch (IllegalArgumentException iae) {
                            logger.error(finalCharset+" failed using "+failoverCharset+" as last chance");
                            try {
                                filename = new String(getPathBytes(file),failoverCharset );
                            } catch (IllegalAccessException ex) {
                                logger.error(ex.getMessage(), ex);
                            }
                        }

                        final Path destFile = Paths.get(destDir.toString(),
                                filename);
                        logger.trace("Extracting file " + filename + " to "
                                + destFile);
                        Files.copy(file, destFile,
                                StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir,
                            BasicFileAttributes attrs) throws IOException {
                        final Path dirToCreate = Paths.get(destDir.toString(),
                                dir.toString());
                        if (Files.notExists(dirToCreate)) {
                            logger.trace("Creating directory " + dirToCreate);
                            Files.createDirectory(dirToCreate);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            return true;

        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            return false;
        } catch (ZipError ze) {
            throw new KoyaServiceException(KoyaErrorCodes.INVALID_ZIP_ARCHIVE, ze);
        }
    }

    private static String determineCharset(String zipPath) throws IOException {
        try (FileSystem zipFileSystem = FileSystems.newFileSystem(
                Paths.get(zipPath), null)) {

            final Path root = zipFileSystem.getPath("/");
            final UniversalDetector detector = new UniversalDetector(null);

            // walk the zip file tree to determine filename encoding
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file,
                        BasicFileAttributes attrs) throws IOException {
                    try {
                        handleData(file);
                    } catch (IllegalAccessException ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir,
                        BasicFileAttributes attrs) throws IOException {
                    try {
                        handleData(dir);
                    } catch (IllegalAccessException ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                    return FileVisitResult.CONTINUE;
                }

                private void handleData(Path p) throws IllegalAccessException {
                    if (p.getFileName() != null) {
                        byte[] b = getPathBytes(p.getFileName());

                        detector.handleData(b, 0, b.length);
                    }
                }
            });

            detector.dataEnd();

            String charsetDetectedName = detector.getDetectedCharset();

            return charsetDetectedName;
        }
    }

    private static byte[] getPathBytes(Path p)
            throws IllegalAccessException {
        return (byte[]) FieldUtils.readDeclaredField(p, "path",
                true);

    }

}
