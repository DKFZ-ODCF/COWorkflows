package de.dkfz.b080.co.common

/**
 * Centralizes all the pathname parsing code and allows to incrementally build up and fill the input table.
 */
class PathBasedInputTableBuilder {

    HashMap<String,Integer> headerMap
    List<Map<String,String>> records

    public MetadataTable getInputTable () {
        MetadataTable newTable = new MetadataTable(headerMap, records)
        newTable.assertValidTable()
        return newTable
    }

    //

    public addFastq(File fastqFile) {

    }

    public addBam(File fastqFile) {

    }


}
