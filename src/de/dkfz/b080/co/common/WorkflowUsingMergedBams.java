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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static de.dkfz.b080.co.files.COConstants.FLAG_EXTRACT_SAMPLES_FROM_OUTPUT_FILES;

/**
 * A basic workflow which uses merged bam files as an input and offers some check routines for those files.
 * Created by michael on 05.05.14.
 */
public abstract class WorkflowUsingMergedBams extends Workflow {

    private Map<DataSet, BamFile[]> foundInputFiles = new LinkedHashMap<>();

    public BamFile[] getInitialBamFiles(ExecutionContext context) {
        //Enable extract samples by default.
        boolean val = context.getConfiguration().getConfigurationValues().getBoolean(FLAG_EXTRACT_SAMPLES_FROM_OUTPUT_FILES, true);
        context.getConfiguration().getConfigurationValues().put(FLAG_EXTRACT_SAMPLES_FROM_OUTPUT_FILES, "" + val, "boolean");

        COProjectsRuntimeService runtimeService = (COProjectsRuntimeService) context.getRuntimeService();
        List<Sample> samples = runtimeService.getSamplesForContext(context);
        List<BamFile> bamsTumorMerged = new LinkedList<>();
        BamFile bamControlMerged = null;
        DataSet dataSet = context.getDataSet();

        BamFile[] found = null;

        synchronized (foundInputFiles) {
            if (!foundInputFiles.containsKey(dataSet)) {
                List<BamFile> allFound = new LinkedList<>();
                for (Sample sample : samples) {
                    BamFile tempBam = ((COProjectsRuntimeService) context.getRuntimeService()).getMergedBamFileForDataSetAndSample(context, sample);
                    if (sample.getType() == Sample.SampleType.CONTROL)
                        bamControlMerged = tempBam;
                    else if (sample.getType() == Sample.SampleType.TUMOR)
                        bamsTumorMerged.add(tempBam);
                }
                allFound.add(bamControlMerged);
                allFound.addAll(bamsTumorMerged);
                foundInputFiles.put(dataSet, allFound.toArray(new BamFile[0]));
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

        //TODO Low priority. There were thoughts to have workflows which support multi-tumor samples, this it not supported by any workflow now.
        if (context.getConfiguration().getConfigurationValues().getBoolean("workflowSupportsMultiTumorSamples", false)) {
            return executeMulti(context, initialBamFiles);
        }
        return execute(context, initialBamFiles[0], initialBamFiles[1]);
    }

    protected abstract boolean execute(ExecutionContext context, BamFile bamControlMerged, BamFile bamTumorMerged);

    private boolean executeMulti(ExecutionContext context, BamFile[] initialBamFiles) {

        boolean result = true;
        BamFile bamControlMerged = initialBamFiles[0];
        for (int i = 1; i < initialBamFiles.length; i++) {
            result &= execute(context, bamControlMerged, initialBamFiles[i]);
        }
        return result;
    }

    @Override
    public boolean checkExecutability(ExecutionContext context) {
        BamFile[] initialBamFiles = getInitialBamFiles(context);
        if (initialBamFiles == null) return false;
        return checkInitialFiles(context, initialBamFiles);
    }
}
