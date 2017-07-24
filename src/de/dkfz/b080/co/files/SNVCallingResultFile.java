package de.dkfz.b080.co.files;

import de.dkfz.roddy.knowledge.files.BaseFile;

/**
 *
 * @author michael
 */
public abstract class SNVCallingResultFile extends BaseFile {

//    protected SNVCallingResultFile(BamFile parentFile) {
//        super(parentFile);
//    }


    public SNVCallingResultFile(ConstructionHelperForBaseFiles helper) {
        super(helper);
    }

    public SNVCallingResultFile(BaseFile parent) {
        super(parent);
    }
}
