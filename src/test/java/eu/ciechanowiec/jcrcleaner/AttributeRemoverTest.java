package eu.ciechanowiec.jcrcleaner;

import lombok.SneakyThrows;
import org.apache.jackrabbit.vault.util.JcrConstants;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class AttributeRemoverTest {

    @ParameterizedTest
    @MethodSource("mustRemoveAttributesArgs")
    @SneakyThrows
    void mustRemoveAttributes(String inputPath, String expectedOutputPath,
                              Collection<String> namesOfAttributesToRemove) {
        File inputFileOriginal = Util.toFile(inputPath);
        File inputFileCopy = Util.copy(inputFileOriginal);
        String inputContentBeforeCleanup = Util.toString(inputFileCopy);
        String expectedOutputContent = Util.toString(expectedOutputPath);
        Set<File> setWithInputFile = Set.of(inputFileCopy);
        AttributeRemover attributeRemover = new AttributeRemover(namesOfAttributesToRemove);
        attributeRemover.removeAttributes(setWithInputFile);
        String inputContentAfterCleanup = Util.toString(inputFileCopy);
        assertAll(
                () -> assertNotEquals(expectedOutputContent, inputContentBeforeCleanup),
                () -> assertEquals(expectedOutputContent, inputContentAfterCleanup)
        );
    }

    static Stream<Arguments> mustRemoveAttributesArgs() {
        Collection<String> customNamesOfAttributesToRemove = Set.of("customos:dalida", "customos:monte");
        Collection<String> narrowedNamesOfAttributesToRemove = Set.of(JcrConstants.JCR_ISCHECKEDOUT);
        return Stream.of(
                arguments("files-2/pair-1/input.xml", "files-2/pair-1/expectedOutput.xml",
                        Collections.emptySet()),
                arguments("files-2/pair-2/input.xml", "files-2/pair-2/expectedOutput.xml",
                        customNamesOfAttributesToRemove),
                arguments("files-2/pair-3/input.xml", "files-2/pair-3/expectedOutput.xml",
                        narrowedNamesOfAttributesToRemove)
        );
    }
}
