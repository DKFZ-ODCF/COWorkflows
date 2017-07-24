package de.dkfz.b080.co.files;

import de.dkfz.roddy.knowledge.files.BaseFile;

/**
 *
 * @author michael
 */
public class FlagstatsFile extends COBaseFile {

//    public FlagstatsFile(BamFile bamFile) {
//        super(bamFile, bamFile.getFileStage());
//    }

    public FlagstatsFile(ConstructionHelperForBaseFiles helper) {
        super(helper);
    }

    public FlagstatsFile(BaseFile parent) {
        super(parent);
    }
}
