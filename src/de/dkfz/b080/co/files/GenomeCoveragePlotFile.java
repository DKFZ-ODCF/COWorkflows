package de.dkfz.b080.co.files;

import de.dkfz.roddy.knowledge.files.BaseFile;

/**
 *
 * @author michael
 */
public class GenomeCoveragePlotFile extends COBaseFile {

//    public GenomeCoveragePlotFile(CoverageTextFile parentFile) {
//        super(parentFile);
//    }
//
//    public GenomeCoveragePlotFile(BamFileGroup group) {
//        super(group, group.getFilesInGroup().get(0).getFileStage());
//    }


    public GenomeCoveragePlotFile(ConstructionHelperForBaseFiles helper) {
        super(helper);
    }

    public GenomeCoveragePlotFile(BaseFile parent) {
        super(parent);
    }

}
