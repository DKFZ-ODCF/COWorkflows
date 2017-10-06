package de.dkfz.b080.co.files;

import de.dkfz.roddy.knowledge.files.BaseFile;

/**
 *
 * @author michael
 */
public class BamIndexFile extends COBaseFile {

//    public BamIndexFile(BamFile bamFile, JobResult jobResult) {
//        super(bamFile, bamFile.getFileStage(), jobResult);
//    }

//    public BamIndexFile(BamFile bamFile) {
//        super(bamFile, bamFile.getFileStage());
//    }

    public BamIndexFile(ConstructionHelperForBaseFiles helper) {
        super(helper);
    }

    public BamIndexFile(BaseFile parent) {
        super(parent);
    }
}
