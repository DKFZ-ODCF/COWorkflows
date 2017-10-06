package de.dkfz.b080.co.files;

import de.dkfz.roddy.knowledge.files.BaseFile;

/**
 *
 * @author michael
 */
public class SNVDeepAnnotationFile extends BaseFile {

//    public SNVDeepAnnotationFile(File path, ExecutionContext executionContext, JobResult creatingJobsResult, List<BaseFile> parentFiles, FileStageSettings settings) {
//        super(path, executionContext, creatingJobsResult, parentFiles, settings);
//    }

    public SNVDeepAnnotationFile(ConstructionHelperForBaseFiles helper) {
        super(helper);
    }

    public SNVDeepAnnotationFile(BaseFile parent) {
        super(parent);
    }
}
