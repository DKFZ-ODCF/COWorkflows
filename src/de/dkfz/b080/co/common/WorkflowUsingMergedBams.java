package de.dkfz.b080.co.common;

import de.dkfz.b080.co.files.BamFile;
import de.dkfz.b080.co.files.COFileStageSettings;
import de.dkfz.b080.co.files.Sample;
import de.dkfz.roddy.StringConstants;
import de.dkfz.roddy.config.RecursiveOverridableMapContainerForConfigurationValues;
import de.dkfz.roddy.core.DataSet;
import de.dkfz.roddy.core.ExecutionContext;
import de.dkfz.roddy.core.ExecutionContextError;
import de.dkfz.roddy.core.Workflow;
import de.dkfz.roddy.knowledge.files.BaseFile;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.dkfz.b080.co.files.COConstants.FLAG_EXTRACT_SAMPLES_FROM_OUTPUT_FILES;

/**
 * A basic workflow which uses merged bam files as an input and offers some check routines for those files.
 * Created by michael on 05.05.14.
 */
public abstract class WorkflowUsingMergedBams extends Workflow {

    public static final String BAMFILE_LIST = "bamfile_list";
    private Map<DataSet, BamFile[]> foundInputFiles = new LinkedHashMap<>();

    public BamFile[] getInitialBamFiles(ExecutionContext context) {
        //Enable extract samples by default.
        RecursiveOverridableMapContainerForConfigurationValues configurationValues = context.getConfiguration().getConfigurationValues();
        boolean val = configurationValues.getBoolean(FLAG_EXTRACT_SAMPLES_FROM_OUTPUT_FILES, true);
        configurationValues.put(FLAG_EXTRACT_SAMPLES_FROM_OUTPUT_FILES, "" + val, "boolean");

        boolean bamfileListIsSet = configurationValues.hasValue(BAMFILE_LIST);
        // There is a method missing in COProjectsRuntimeService. This fix will ONLY work, when sample_list is set!
        List<String> samplesPassedInConfig = Arrays.asList(configurationValues.getString("sample_list", "").split("[;]"));
        boolean sampleListIsSet = samplesPassedInConfig != null && samplesPassedInConfig.size() > 0;

        COProjectsRuntimeService runtimeService = (COProjectsRuntimeService) context.getRuntimeService();
        List<Sample> samples = runtimeService.getSamplesForRun(context);
        BamFile bamTumorMerged = null;
        BamFile bamControlMerged = null;
        DataSet dataSet = context.getDataSet();

        BamFile[] found = null;

        synchronized (foundInputFiles) {
            if (!foundInputFiles.containsKey(dataSet)) {
                if (bamfileListIsSet) {
                    if (!sampleListIsSet) {
                        context.addErrorEntry(ExecutionContextError.EXECUTION_NOINPUTDATA.expand("Bam files were set in configuration but the bamfile_list value is missing."));

                    } else {
                        List<String> bamFiles = Arrays.asList(configurationValues.getString(BAMFILE_LIST, "").split(StringConstants.SPLIT_SEMICOLON));
                        for (int i = 0; i < bamFiles.size(); i++) {
                            File path = new File(bamFiles.get(i));
                            Sample sample = ((COProjectsRuntimeService) context.getRuntimeService()).getSamplesForRun(context).get(i);
                            if (sample.getType() == Sample.SampleType.CONTROL)
                                bamControlMerged = new BamFile(new BaseFile.ConstructionHelperForSourceFiles(path, context, new COFileStageSettings(sample, dataSet), null));
                            else if (sample.getType() == Sample.SampleType.TUMOR)
                                bamTumorMerged = (new BamFile(new BaseFile.ConstructionHelperForSourceFiles(path, context, new COFileStageSettings(sample, dataSet), null)));

                        }
                        // This code block is from R2.3 / new COWorkflows. However, if not neccessary, I will not port it! It is used, when samples were not passed with sample_list
//                    for (String bf : bamFiles) {
//                        File path = new File(bf);
//                        Sample sample = COProjectsRuntimeService.extractSamplesFromFilenames([path], context)[0];
//                        if (sample.getType() == Sample.SampleType.CONTROL)
//                            bamControlMerged = new BasicBamFile(new BaseFile.ConstructionHelperForSourceFiles(path, context, new COFileStageSettings(sample, dataSet), null));
//                        else if (sample.getType() == Sample.SampleType.TUMOR)
//                            bamsTumorMerged.add(new BasicBamFile(new BaseFile.ConstructionHelperForSourceFiles(path, context, new COFileStageSettings(sample, dataSet), null)));
//                    }
                    }
                } else {

                    for (Sample sample : samples) {
                        BamFile tempBam = ((COProjectsRuntimeService) context.getRuntimeService()).getMergedBamFileForDataSetAndSample(context, sample);
                        if (sample.getType() == Sample.SampleType.CONTROL)
                            bamControlMerged = tempBam;
                        else if (sample.getType() == Sample.SampleType.TUMOR)
                            bamTumorMerged = tempBam;
                    }
                }
                foundInputFiles.put(dataSet, new BamFile[]{bamControlMerged, bamTumorMerged});
            }
            found = foundInputFiles.get(dataSet);
            if (found != null && found[0] != null && found[0].getExecutionContext() != context) {
                BamFile[] copy = new BamFile[found.length];
                for (int i = 0; i < found.length; i++) {
                    if (found[i] == null) continue;
                    copy[i] = new BamFile(new BaseFile.ConstructionHelperForSourceFiles(found[i].getPath(), context, found[i].getFileStage().copy(), null));
                    copy[i].setAsSourceFile();
                }
                found = copy;
            }
        }
        return found;
    }

    public boolean checkInitialFiles(ExecutionContext context, BamFile[] initialBamFiles) {
        BamFile bamControlMerged = initialBamFiles[0];
        BamFile bamTumorMerged = initialBamFiles[1];
        if (bamControlMerged == null || bamTumorMerged == null) {
            if (bamControlMerged == null)
                context.addErrorEntry(ExecutionContextError.EXECUTION_NOINPUTDATA.expand("Control bam is missing"));
            if (bamTumorMerged == null)
                context.addErrorEntry(ExecutionContextError.EXECUTION_NOINPUTDATA.expand("Tumor bam is missing"));
            return false;
        }
        return true;
    }


    @Override
    public boolean execute(ExecutionContext context) {
        BamFile[] initialBamFiles = getInitialBamFiles(context);
        if (!checkInitialFiles(context, initialBamFiles))
            return false;

        BamFile bamControlMerged = initialBamFiles[0];
        BamFile bamTumorMerged = initialBamFiles[1];
        return execute(context, bamControlMerged, bamTumorMerged);
    }

    protected abstract boolean execute(ExecutionContext context, BamFile bamControlMerged, BamFile bamTumorMerged);

    @Override
    public boolean checkExecutability(ExecutionContext context) {
        BamFile[] initialBamFiles = getInitialBamFiles(context);
        if (initialBamFiles == null) return false;
        return checkInitialFiles(context, initialBamFiles);
    }
}
