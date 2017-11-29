package de.dkfz.b080.co.files

import de.dkfz.roddy.knowledge.files.BaseFile.ConstructionHelperForBaseFiles

class ChromosomeDiffValueFile extends COBaseFile {

    ChromosomeDiffValueFile(BamFile parentFile) {
        super(parentFile);
    }

    ChromosomeDiffValueFile(ConstructionHelperForBaseFiles helper) {
        super(helper)
    }

}
