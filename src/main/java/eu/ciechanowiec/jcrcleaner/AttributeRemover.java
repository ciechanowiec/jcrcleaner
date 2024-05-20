package eu.ciechanowiec.jcrcleaner;

import com.day.cq.replication.ReplicationStatus;
import com.day.cq.wcm.api.NameConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.vault.util.JcrConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
class AttributeRemover {

    @SuppressWarnings({"deprecation", "FieldCanBeLocal"})
    private static final Collection<String> NAMES_OF_DEFAULT_ATTRIBUTES_TO_REMOVE = Set.of(
            ReplicationStatus.NODE_PROPERTY_LAST_REPLICATED,          // cq:lastReplicated
            ReplicationStatus.NODE_PROPERTY_LAST_REPLICATED_BY,       // cq:lastReplicatedBy
            ReplicationStatus.NODE_PROPERTY_LAST_REPLICATION_ACTION,  // cq:lastReplicationAction
            NameConstants.PN_PAGE_LAST_MOD,                           // cq:lastModified
            NameConstants.PN_PAGE_LAST_MOD_BY,                        // cq:lastModifiedBy
            NameConstants.PN_PAGE_LAST_PUBLISHED,                     // cq:lastPublished
            NameConstants.PN_PAGE_LAST_PUBLISHED_BY,                  // cq:lastPublishedBy
            JcrConstants.JCR_LASTMODIFIED,                            // jcr:lastModified
            JcrConstants.JCR_LAST_MODIFIED_BY,                        // jcr:lastModifiedBy
            JcrConstants.JCR_CREATED,                                 // jcr:created
            JcrConstants.JCR_CREATED_BY,                              // jcr:createdBy
            JcrConstants.JCR_ISCHECKEDOUT,                            // jcr:isCheckedOut
            JcrConstants.JCR_UUID                                     // jcr:uuid
    );

    private final Collection<String> namesOfAttributesToRemove;

    /**
     * @param namesOfAttributesToRemove names of attributes that should be removed; if empty, then
     *                                  the default set of the names, specified in
     *                                  {@link AttributeRemover#NAMES_OF_DEFAULT_ATTRIBUTES_TO_REMOVE}, is used
     */
    AttributeRemover(Collection<String> namesOfAttributesToRemove) {
        boolean thereArePassedNames = !namesOfAttributesToRemove.isEmpty();
        if (thereArePassedNames) {
            Set<String> passedNamesAsMutableSet = new HashSet<>(namesOfAttributesToRemove);
            this.namesOfAttributesToRemove = Collections.unmodifiableSet(passedNamesAsMutableSet);
        } else {
            this.namesOfAttributesToRemove = NAMES_OF_DEFAULT_ATTRIBUTES_TO_REMOVE;
        }
        log.info("The attributes with the following names are set as subject for removal: {}",
                  this.namesOfAttributesToRemove);
    }

    void removeAttributes(Collection<File> filesForCleaning) {
        int numOfFilesToRemoveAttributesFrom = filesForCleaning.size();
        log.info("Removing attributes for {} files will be performed now", numOfFilesToRemoveAttributesFrom);
        filesForCleaning.forEach(this::removeAttributes);
        log.info("Finished removing attributes for {} files", numOfFilesToRemoveAttributesFrom);
    }

    @SneakyThrows
    private void removeAttributes(File fileForCleaning) {
        try {
            log.debug("Removing attributes for a file at this path: {}", fileForCleaning);
            Path filePath = fileForCleaning.toPath();
            String originalContent = Files.readString(filePath);
            @SuppressWarnings("ChainedMethodCall")
            String newContent = namesOfAttributesToRemove.stream()
                                                         .reduce(originalContent, this::removeAttributes);
            newContent = prettify(newContent);
            Files.writeString(filePath, newContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            log.error("Unable to remove attributes for a file at this path: {}", fileForCleaning);
            throw exception;
        }
    }

    private String prettify(String contentToPrettify) {
        log.debug("Removing empty lines");
        String prettifiedContent = contentToPrettify.replaceAll("(?m)^[ \t]*\r?\n", "");
        log.debug("Squashing dangling '>' with the previous line");
        prettifiedContent = prettifiedContent.replaceAll("\n[ \t]*>\r?\n", ">\n");
        log.debug("Squashing dangling '/>' with the previous line");
        prettifiedContent = prettifiedContent.replaceAll("\n[ \t]*/>\r?\n", "/>\n");
        log.debug("Removing unnecessary whitespaces at the end of the blocks before '>'");
        prettifiedContent = prettifiedContent.replaceAll("\"\\s+>\r?\n", "\">\n");
        log.debug("Removing unnecessary whitespaces at the end of the blocks before '/>'");
        prettifiedContent = prettifiedContent.replaceAll("\"\\s+/>\r?\n", "\"/>\n");
        log.debug("Removing trailing whitespaces");
        prettifiedContent = prettifiedContent.replaceAll("\\s+\r?\n", "\n");
        return prettifiedContent;
    }

    @SuppressWarnings("ChainedMethodCall")
    private String removeAttributes(CharSequence xmlContentToChange, String attributeName) {
        log.debug("Removing attributes that have this name: '{}'", attributeName);
        return Pattern.compile(attributeName + "=\"[^\"]*\"", Pattern.MULTILINE)
                      .matcher(xmlContentToChange)
                      .replaceAll(StringUtils.EMPTY);
    }
}
