package de.dkfz.b080.co.files;

import de.dkfz.roddy.knowledge.files.BaseFile;
import de.dkfz.roddy.knowledge.methods.GenericMethod;

/**
 * @author michael
 */
public class SNVAnnotationFile extends BaseFile {

    private BamFile parentFile;

    public SNVAnnotationFile(ConstructionHelperForBaseFiles helper) {
        super(helper);
    }

    public SNVAnnotationFile(BaseFile parent, BamFile parentFile) {
        super(parent);
        this.parentFile = parentFile;
    }

    public VCFFileWithCheckpointFile annotate() {
        VCFFileWithCheckpointFile file = GenericMethod.callGenericTool(COConstants.TOOL_SNV_ANNOTATION, this, parentFile);
        return file;
    }

    public VCFFileWithCheckpointFile deepAnnotate() {
        VCFFileWithCheckpointFile file = GenericMethod.callGenericTool(COConstants.TOOL_SNV_DEEP_ANNOTATION, this, "PIPENAME=SNV_DEEPANNOTATION");
        return file;
    }

    public VCFFileWithCheckpointFile filter(SNVAnnotationFile rawVCFFile, BamFile tumorBamFile) {
        VCFFileWithCheckpointFile file = GenericMethod.callGenericTool(COConstants.TOOL_SNV_FILTER, this, rawVCFFile, tumorBamFile, "SNVFILE_PREFIX=snvs_");
        return file;
    }
}
