package de.dkfz.b080.co.files;

import de.dkfz.roddy.core.ExecutionContext;
import de.dkfz.roddy.execution.jobs.Job;
import de.dkfz.roddy.execution.jobs.JobResult;
import de.dkfz.roddy.knowledge.files.BaseFile;
import de.dkfz.roddy.knowledge.files.FileGroup;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by michael on 11.06.14.
 */
public class CoverageWindowsFileGroupByChromosome extends FileGroup {

    private Map<String, CoverageWindowsFile> files;

    public CoverageWindowsFileGroupByChromosome(Map<String, CoverageWindowsFile> files) {
        super(new LinkedList<>(files.values()));
        this.files = files;
    }

    public Map<String, CoverageWindowsFile> getFiles() {
        return files;
    }

    public CoverageWindowsFileAnnotationResult annotate() {
        List<TextFile> listOfFiles = new LinkedList<>();
        List<BaseFile> filesToCheck = new LinkedList<>();
        for (String chrIndex : files.keySet()) {
            TextFile tf = new TextFile(files.get(chrIndex));
            tf.overrideFilenameUsingSelectionTag("annotatedCoverage");
            String path = tf.getAbsolutePath();
            tf.setPath(new File(path.replace("#CHROMOSOME_INDEX#", chrIndex)));
            listOfFiles.add(tf);
            filesToCheck.add(tf);
        }
        TextFile genderFile = new TextFile(files.get("1"));
        genderFile.overrideFilenameUsingSelectionTag("genderFile");
        filesToCheck.add(genderFile);

        ExecutionContext run = getExecutionContext();
        Map<String, Object> parameters = run.getDefaultJobParameters(COConstants.TOOL_ANNOTATE_COV_WIN);
        parameters.put("FILENAME_SEX", genderFile.getAbsolutePath());

        Job job = new Job(run, run.createJobName((BaseFile)getFilesInGroup().get(0), COConstants.TOOL_ANNOTATE_COV_WIN, true), COConstants.TOOL_ANNOTATE_COV_WIN, null, parameters, (List<BaseFile>)getFilesInGroup(), filesToCheck);
        JobResult jobResult = job.run();
        for (BaseFile baseFile : filesToCheck) {
            baseFile.setCreatingJobsResult(jobResult);
        }

        return new CoverageWindowsFileAnnotationResult(listOfFiles, genderFile);
    }
}
