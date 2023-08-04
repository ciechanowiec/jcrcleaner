package eu.ciechanowiec.jcrcleaner;

import lombok.SneakyThrows;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

final class Util {

    private Util() throws InstantiationException {
        throw new InstantiationException("Utility class. Instantiation is forbidden");
    }

    @SneakyThrows
    static File copy(File fileToCopy) {
        Path fileToCopyPath = fileToCopy.toPath();
        String fileToCopyPathAsString = fileToCopyPath.toString();
        Path copiedFilePathBeforeCopy = Paths.get(fileToCopyPathAsString + "-copy");
        Path copiedFilePathAfterCopy = Files.copy(fileToCopyPath, copiedFilePathBeforeCopy,
                                                  StandardCopyOption.REPLACE_EXISTING);
        return copiedFilePathAfterCopy.toFile();
    }

    @SneakyThrows
    static File toFile(String relPath) {
        Class<? extends Util> currentClass = Util.class;
        ClassLoader currentClassLoader = currentClass.getClassLoader();
        URL fileURL = currentClassLoader.getResource(relPath);
        Objects.requireNonNull(fileURL);
        URI fileURI = fileURL.toURI();
        return new File(fileURI);
    }

    @SneakyThrows
    static String toString(String relPathToFileToConvert) {
        File fileToConvert = toFile(relPathToFileToConvert);
        return toString(fileToConvert);
    }

    @SneakyThrows
    static String toString(File fileToConvert) {
        Path filePath = fileToConvert.toPath();
        return Files.readString(filePath);
    }
}
