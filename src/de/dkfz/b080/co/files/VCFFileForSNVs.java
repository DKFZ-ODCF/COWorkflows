package de.dkfz.b080.co.files;

import de.dkfz.roddy.knowledge.files.BaseFile;
/**
 *
 * @author michael
 */
public class VCFFileForSNVs extends SNVCallingResultFile {
//    public VCFFileForSNVs(BamFile parentFile) {
//        super(parentFile);
//    }


    public VCFFileForSNVs(ConstructionHelperForBaseFiles helper) {
        super(helper);
    }

    public VCFFileForSNVs(BaseFile parent) {
        super(parent);
    }
}
