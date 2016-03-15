package de.dkfz.b080.co.common

@groovy.transform.CompileStatic
abstract class InputFileInformationExtractor {

    abstract String getSampleName()

    abstract String getLibraryId ()

    abstract String getPlatformId ()

    abstract String getSampleType ()

    abstract String getPairingType ()

}
