package de.dkfz.b080.co.common

import de.dkfz.roddy.StringConstants
import de.dkfz.roddy.config.RecursiveOverridableMapContainerForConfigurationValues
import de.dkfz.roddy.core.ExecutionContext
import static de.dkfz.b080.co.files.COConstants.*

@groovy.transform.CompileStatic
class COConfig {

    public ExecutionContext context

    COConfig(ExecutionContext context) {
        this.context = context
    }

    // This is used so often, it should maybe be part of ExecutionContext.
    RecursiveOverridableMapContainerForConfigurationValues getConfigValues() {
        return context.getConfiguration().getConfigurationValues()
    }

    void setConfig(String flagName, String value, String typeName) {
        configValues.put(flagName, value, typeName)
    }

    boolean getExtractSamplesFromFastqFileList() {
        return !getFastqList().isEmpty()
    }

    boolean getExtractSamplesFromOutputFiles() {
        return configValues.getBoolean(FLAG_EXTRACT_SAMPLES_FROM_OUTPUT_FILES, false)
    }

    boolean getEnforceAtomicSampleName() {
        return configValues.getBoolean(FLAG_ENFORCE_ATOMIC_SAMPLE_NAME, false)
    }

    boolean getExtractSamplesFromBamList() {
        return !getBamList().isEmpty()
    }

    private List<String> checkAndSplitListFromConfig(String listID) {
        String list = configValues.getString(listID, null);
        if(list)
            return list.split(StringConstants.SPLIT_SEMICOLON) as List<String>
        return [];
    }

    List<String> getFastqList() {
        return checkAndSplitListFromConfig("fastq_list");
    }

    List<String> getBamList() {
        return checkAndSplitListFromConfig("bamfile_list");
    }

    String getSequenceDirectory() {
        return configValues.get("sequenceDirectory").toFile(context).getAbsolutePath()
    }

    String getAlignmentFolderName() {
        return configValues.getString(CVALUE_ALIGNMENT_DIRECTORY_NAME, "alignment")
    }

    String[] getMergedBamSuffixList() {
        return configValues.get("mergedBamSuffixList", "merged.bam.dupmarked.bam").toString().split(StringConstants.COMMA)
    }

    boolean getUseMergedBamsFromInputDirectory() {
        return configValues.getBoolean("useMergedBamsFromInputDirectory", false)
    }

    boolean getSearchMergedBamFilesWithPID() {
        return configValues.getBoolean("searchMergedBamFilesWithPID", false)
    }

    List<String> getPossibleControlSampleNamePrefixes() {
        return configValues.get("possibleControlSampleNamePrefixes").toStringList(" ", ["(", ")"] as String[])
    }

    List<String> getPossibleTumorSampleNamePrefixes() {
        return configValues.get("possibleTumorSampleNamePrefixes").toStringList(" ", ["(", ")"] as String[])
    }

}
