package eu.ciechanowiec.jcrcleaner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.vault.util.Constants;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Mojo(name = CleanerMojo.GOAL_NAME, defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
@SuppressWarnings({"unused", "NullableProblems"})
class CleanerMojo extends AbstractMojo {

    static final String GOAL_NAME = "clean-jcr";

    /**
     * {@code true} if the plugin should be enabled and perform cleaning when executed; {@code false} otherwise.
     * By default, this value is {@code true}, which means that by default the plugin is enabled.
     */
    @Parameter(property = "isEnabled", defaultValue = "true")
    @SuppressWarnings({"unused", "InstanceVariableMayNotBeInitialized"})
    private boolean isEnabled;

    /**
     * Regex for file names. Only content of files whose names match the specified regex will be subject to cleaning.
     * The default value is <i>.content.xml</i> ({@link Constants#DOT_CONTENT_XML}), which is the default name for
     * files in FileVault Document View (DocView) format.
     */
    @SuppressWarnings({"unused", "InstanceVariableMayNotBeInitialized"})
    @Parameter(property = "fileNameRegex", defaultValue = Constants.DOT_CONTENT_XML)
    private String fileNameRegex;

    /**
     * Regex for absolute paths to exclude. Content of all files whose absolute paths match the specified regex
     * will be excluded from cleaning. By default, this value is not specified, which means that by default
     * there are no files excluded from cleaning on the base of their absolute paths.
     */
    @Nullable
    @SuppressWarnings("unused")
    @Parameter(property = "excludedAbsPathRegex")
    private String excludedAbsPathRegex;

    /**
     * Names of properties of JCR nodes that should be removed from the matched files.
     * Only properties that have the names specified in this collection will be removed.
     * By default, those names are:
     * <ol>
     *   <li>{@code cq:lastReplicated}</li>
     *   <li>{@code cq:lastReplicatedBy}</li>
     *   <li>{@code cq:lastReplicationAction}</li>
     *   <li>{@code cq:lastModified}</li>
     *   <li>{@code cq:lastModifiedBy}</li>
     *   <li>{@code cq:lastPublished}</li>
     *   <li>{@code cq:lastPublishedBy}</li>
     *   <li>{@code jcr:lastModified}</li>
     *   <li>{@code jcr:lastModifiedBy}</li>
     *   <li>{@code jcr:created}</li>
     *   <li>{@code jcr:createdBy}</li>
     *   <li>{@code jcr:isCheckedOut}</li>
     *   <li>{@code jcr:uuid}</li>
     * </ol>
     */
    @Nullable
    @Parameter(property = "namesOfPropertiesToRemove")
    @SuppressWarnings("unused")
    private Collection<String> namesOfPropertiesToRemove;

    /**
     * The current Maven project upon which the plugin is being executed.
     */
    @Nonnull
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    @SuppressWarnings({"unused", "InstanceVariableMayNotBeInitialized", "NotNullFieldNotInitialized", "NullableProblems"})
    private MavenProject mavenProject;

    @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
    @SuppressWarnings("squid:S2637")
    CleanerMojo() {
        // For fields injection
    }

    CleanerMojo(String fileNameRegex, String excludedAbsPathRegex, MavenProject mavenProject,
                boolean isEnabled, Collection<String> namesOfPropertiesToRemove) {
        // For tests
        this.fileNameRegex = fileNameRegex;
        this.excludedAbsPathRegex = excludedAbsPathRegex;
        this.mavenProject = mavenProject;
        this.isEnabled = isEnabled;
        this.namesOfPropertiesToRemove = Set.copyOf(namesOfPropertiesToRemove);
    }

    public void execute() {
        Timer start = Timer.start();
        log.info("Started plugin execution");
        log.info("[injected parameters] fileNameRegex: '{}', excludedAbsPathRegex: '{}', isEnabled: '{}', " +
                 "namesOfPropertiesToRemove: {}",
                  fileNameRegex, excludedAbsPathRegex, isEnabled, namesOfPropertiesToRemove);
        if (isEnabled) {
            log.info("The plugin is enabled. Further actions will be performed now");
            File baseDir = mavenProject.getBasedir();
            log.info("Base directory: {}", baseDir);
            String excludedAbsPathRegexNonNull = Optional.ofNullable(excludedAbsPathRegex)
                                                         .orElse(StringUtils.EMPTY);
            Collection<String> namesOfPropertiesToRemoveNonNull = Optional.ofNullable(namesOfPropertiesToRemove)
                    .orElse(Collections.emptySet());
            FileProvider fileProvider = new FileProvider(fileNameRegex, excludedAbsPathRegexNonNull, baseDir);
            Collection<File> filesToChange = fileProvider.provide();
            AttributeRemover attributeRemover = new AttributeRemover(namesOfPropertiesToRemoveNonNull);
            attributeRemover.removeAttributes(filesToChange);
        } else {
            log.info("The plugin is disabled. No further actions will be performed");
        }
        String executionTime = start.renderedTimeFromStart();
        log.info("Plugin execution finished. Execution time: {}", executionTime);
    }
}
