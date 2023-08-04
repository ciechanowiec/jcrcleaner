package eu.ciechanowiec.jcrcleaner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@ToString
class FileProvider {

    private final String fileNameRegex;
    private final String excludedAbsPathRegex;
    private final File dirWithFiles;

    @SuppressFBWarnings("MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR")
    FileProvider(String fileNameRegex, String excludedAbsPathRegex, File dirWithFiles) {
        this.fileNameRegex = fileNameRegex;
        this.excludedAbsPathRegex = excludedAbsPathRegex;
        this.dirWithFiles = dirWithFiles;
        log.debug("File Provider initialized: {}", this);
    }

    Collection<File> provide() {
        Collection<File> filesFilteredByName = provideFilesMatchingNames();
        return excludeByExcludedAbsPath(filesFilteredByName, excludedAbsPathRegex);
    }

    private Collection<File> provideFilesMatchingNames() {
        log.debug("Requested provisioning of files matching this name regex: '{}'", fileNameRegex);
        IOFileFilter fileNameRegexFilter = new RegexFileFilter(fileNameRegex);
        IOFileFilter nameAndTypeFilter = new AndFileFilter(fileNameRegexFilter, FileFileFilter.INSTANCE);
        Collection<File> filesFilteredByName = FileUtils.listFiles(dirWithFiles, nameAndTypeFilter, TrueFileFilter.TRUE);
        boolean isDebugEnabled = log.isDebugEnabled();
        if (isDebugEnabled) {
            @SuppressWarnings("DuplicatedCode")
            int numOfFilesFilteredByName = filesFilteredByName.size();
            Collection<String> absPaths = toAbsPaths(filesFilteredByName);
            String absPathsAsString = String.join(StringUtils.LF, absPaths);
            boolean thereAreAbsPaths = !absPathsAsString.isBlank();
            absPathsAsString = thereAreAbsPaths ? StringUtils.LF + absPathsAsString : "[]";
            log.debug("Found {} file(s) matching this name regex: '{}'. Absolute paths: {}",
                       numOfFilesFilteredByName, fileNameRegex, absPathsAsString);
        }
        return filesFilteredByName;
    }

    @SuppressWarnings("DuplicatedCode")
    private Collection<File> excludeByExcludedAbsPath(Collection<File> filesToFilter, String excludedAbsPathRegex) {
        boolean isDebugEnabled = log.isDebugEnabled();
        if (isDebugEnabled) {
            int numOfFilesToFilter = filesToFilter.size();
            Collection<String> absPaths = toAbsPaths(filesToFilter);
            String absPathsAsString = String.join(StringUtils.LF, absPaths);
            boolean thereAreAbsPaths = !absPathsAsString.isBlank();
            absPathsAsString = thereAreAbsPaths ? StringUtils.LF + absPathsAsString : "[]";
            log.debug("Requested to filter files by this regex absolute path: '{}'. Number of files to filter: {}. " +
                      "Paths of files to be filtered: {}",
                       excludedAbsPathRegex, numOfFilesToFilter, absPathsAsString);
        }
        boolean thereIsNoRegex = excludedAbsPathRegex.isBlank();
        if (thereIsNoRegex) {
            log.debug("Absolute path regex is blank. No filtering will be performed");
            return filesToFilter;
        }
        Set<File> filteredFiles = filesToFilter.stream()
                .filter(fileToFilter -> !matchesAbsPath(fileToFilter, excludedAbsPathRegex))
                .collect(Collectors.toSet());
        if (isDebugEnabled) {
            int numberOfFilteredFiles = filteredFiles.size();
            Collection<String> absPaths = toAbsPaths(filteredFiles);
            String absPathsAsString = String.join(StringUtils.LF, absPaths);
            boolean thereAreAbsPaths = !absPathsAsString.isBlank();
            absPathsAsString = thereAreAbsPaths ? StringUtils.LF + absPathsAsString : "[]";
            log.debug("Number of files after filtering by regex ('{}'): {}. Paths of those files: {}",
                       excludedAbsPathRegex, numberOfFilteredFiles, absPathsAsString);
        }
        return filteredFiles;
    }

    private boolean matchesAbsPath(File fileToCheck, String absPathToMatchRegex) {
        String fileActualAbsPath = fileToCheck.getAbsolutePath();
        return fileActualAbsPath.matches(absPathToMatchRegex);
    }

    private Collection<String> toAbsPaths(Collection<File> filesToConvert) {
        return filesToConvert.stream()
                             .map(File::getAbsolutePath)
                             .collect(Collectors.toSet());
    }
}
