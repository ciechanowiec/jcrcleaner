package eu.ciechanowiec.jcrcleaner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.vault.util.Constants;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FileProviderTest {

    private static final String FILES_DIR_NAME = "files-1";

    @SneakyThrows
    @MethodSource("mustProvideFilesArgs")
    @ParameterizedTest
    void mustProvideFiles(String fileNameRegex, String excludedAbsPathRegex, int expectedNumOfFiles) {
        File baseDir = Util.toFile(FILES_DIR_NAME);
        FileProvider fileProvider = new FileProvider(fileNameRegex, excludedAbsPathRegex, baseDir);
        Collection<File> providedFiles = fileProvider.provide();
        int actualNumOfFiles = providedFiles.size();
        assertEquals(expectedNumOfFiles, actualNumOfFiles);
    }

    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    private static Stream<Arguments> mustProvideFilesArgs() {
        return Stream.of(
                arguments(Constants.DOT_CONTENT_XML, StringUtils.EMPTY, 3),
                arguments(Constants.DOT_CONTENT_XML, " ", 3),
                arguments("unexistingFileName", StringUtils.EMPTY, 0),
                arguments("text-[0-9].txt", StringUtils.EMPTY, 3),
                arguments("text-2.txt", StringUtils.EMPTY, 1),
                arguments(Constants.DOT_CONTENT_XML, ".*nested-dir-(1|2).*", 1)
        );
    }
}
