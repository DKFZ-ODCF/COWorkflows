package de.dkfz.b080.co.common

import de.dkfz.roddy.execution.io.BaseMetadataTable
import groovy.transform.CompileStatic

import static de.dkfz.b080.co.files.COConstants.*

/**
 * MetadataTable keeps an overview over all factors relevant for guiding the analyses, such as PID, SampleName / TissueType,
 * Library ID, FastQ file, Read Number. Plugins should subclass and override the getMandatoryColumns() and
 * getOptionalColumns() methods.
 */
@CompileStatic
public class MetadataTable  extends BaseMetadataTable {

    public MetadataTable(BaseMetadataTable baseMetadataTable) {
        super(baseMetadataTable);
    }

    private void assertUniqueFastq() {
        // ... what about BAMs?
        Map<String, Integer> tooFrequentFiles = records.countBy {
            it.get(INPUT_TABLE_FILE)
        }.findAll { file, count ->
            count > 1
        }
        if (tooFrequentFiles.size() > 0) {
            throw new RuntimeException("Files occur too often in input table: ${tooFrequentFiles}")
        }
    }

    @Override
    BaseMetadataTable subsetByColumn(String columnName, String value) {
        return new MetadataTable(super.subsetByColumn(columnName, value));
    }

    public MetadataTable subsetBySample(String sampleName) {
        return (MetadataTable)subsetByColumn(INPUT_TABLE_MERGECOL_NAME, sampleName);
    }

    public MetadataTable subsetByRun(String runId) {
        return (MetadataTable)subsetByColumn(INPUT_TABLE_RUNCOL_NAME, runId);
    }

    public MetadataTable subsetByLibrary(String library) {
        return (MetadataTable)subsetByColumn(INPUT_TABLE_MARKCOL_NAME, library);
    }

    public List<String> listSampleNames() {
        return listColumn(INPUT_TABLE_MERGECOL_NAME).unique()
    }

    public List<String> listRunIDs() {
        return listColumn(INPUT_TABLE_RUNCOL_NAME).unique()
    }

    public List<String> listLibraries() {
        return listColumn(INPUT_TABLE_MARKCOL_NAME).unique()
    }

}
