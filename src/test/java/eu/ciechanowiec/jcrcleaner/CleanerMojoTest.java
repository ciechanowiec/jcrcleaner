package eu.ciechanowiec.jcrcleaner;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.vault.util.Constants;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CleanerMojoTest {

    @Mock
    private MavenProject mavenProject;
    private CleanerMojo cleanerMojo;
    private TestLogger cleanerMojoLog;
    private TestLogger attributeRemoverLog;
    private TestLogger fileProviderLog;

    @SneakyThrows
    @BeforeEach
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    void setUp() {
        cleanerMojoLog = TestLoggerFactory.getTestLogger(CleanerMojo.class);
        attributeRemoverLog = TestLoggerFactory.getTestLogger(AttributeRemover.class);
        fileProviderLog = TestLoggerFactory.getTestLogger(FileProvider.class);
        cleanerMojoLog.clearAll();
        attributeRemoverLog.clearAll();
        fileProviderLog.clearAll();
        File baseDir = Util.toFile("files-1");
        Path baseDirPath = baseDir.toPath();
        String baseDirPathAsString = baseDirPath.toString();
        String baseDirCopyPathAsString = baseDirPathAsString + "-copy";
        File baseDirCopy = new File(baseDirCopyPathAsString);
        if (baseDirCopy.exists()) {
            baseDirCopy.delete();
        }
        FileUtils.copyDirectory(baseDir, baseDirCopy);
        when(mavenProject.getBasedir()).thenReturn(baseDirCopy);
        cleanerMojo = new CleanerMojo(Constants.DOT_CONTENT_XML, StringUtils.EMPTY,
                                      mavenProject, true, Collections.emptySet());
    }

    @Test
    @SuppressWarnings("MagicNumber")
    @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
    void mustExecute() {
        cleanerMojo.execute();
        ImmutableList<LoggingEvent> cleanerMojoLogEvents = cleanerMojoLog.getAllLoggingEvents();
        ImmutableList<LoggingEvent> attributeRemoverLogEvents = attributeRemoverLog.getAllLoggingEvents();
        ImmutableList<LoggingEvent> fileProviderLogEvents = fileProviderLog.getAllLoggingEvents();
        int cleanerMojoLogEventsSize = cleanerMojoLogEvents.size();
        int attributeRemoverLogEventsSize = attributeRemoverLogEvents.size();
        int fileProviderLogEventsSize = fileProviderLogEvents.size();
        assertAll(
                () -> assertEquals(5, cleanerMojoLogEventsSize),
                () -> assertEquals(60, attributeRemoverLogEventsSize),
                () -> assertEquals(5, fileProviderLogEventsSize)
        );
    }
}
