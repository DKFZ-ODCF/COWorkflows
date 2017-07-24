package de.dkfz.b080.co.files;

import de.dkfz.roddy.knowledge.files.BaseFile;

/**
 *
 * @author michael
 */
public class AlignedSequenceFile extends COBaseFile {
//    public AlignedSequenceFile(LaneFile parentFile) {
//        super(parentFile, parentFile.getFileStage());
//        setAsTemporaryFile();
//    }


    public AlignedSequenceFile(ConstructionHelperForBaseFiles helper) {
        super(helper);
        setAsTemporaryFile();
    }

    public AlignedSequenceFile(BaseFile parent) {
        super(parent);
        setAsTemporaryFile();
    }
}
