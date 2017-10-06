package de.dkfz.b080.co.files;

import de.dkfz.roddy.knowledge.files.BaseFile;

/**
 *
 * @author michael
 */
public class IndelDeepAnnotationFile extends BaseFile {

//    public IndelDeepAnnotationFile(File path, ExecutionContext executionContext, JobResult creatingJobsResult, List<BaseFile> parentFiles, FileStageSettings settings) {
//        super(path, executionContext, creatingJobsResult, parentFiles, settings);
//    }

    public IndelDeepAnnotationFile(ConstructionHelperForBaseFiles helper) {
        super(helper);
    }

    public IndelDeepAnnotationFile(BaseFile parent) {
        super(parent);
    }
}
