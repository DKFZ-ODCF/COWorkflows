package de.dkfz.b080.co.files;

import de.dkfz.roddy.knowledge.files.BaseFile;

/**
 */
public abstract class COBaseFile<COFileStageSettings> extends BaseFile {
//    public COBaseFile(FileGroup parentFileGroup, FileStageSettings settings, JobResult jobResult) {
//        super(parentFileGroup, settings, jobResult);
//        super()
//    }

//    public COBaseFile(FileGroup parentFileGroup, FileStageSettings settings) {
//        super(parentFileGroup, settings);
//    }
//
//    public COBaseFile(BaseFile parentFile, FileStageSettings fileStage, JobResult jobResult) {
//        super(parentFile, fileStage, jobResult);
//    }
//
//    public COBaseFile(BaseFile parentFile) {
//        super(parentFile);
//    }
//
//    public COBaseFile(BaseFile parentFile, FileStageSettings settings) {
//        super(parentFile, settings);
//    }
//
//    public COBaseFile(File path, ExecutionContext executionContext, JobResult jobResult, List<BaseFile> parentFiles, FileStageSettings settings) {
//        super(path, executionContext, jobResult, parentFiles, settings);
//    }


    public COBaseFile(ConstructionHelperForBaseFiles helper) {
        super(helper);
    }

    public COBaseFile(BaseFile parent) {
        super(parent);
    }
}
