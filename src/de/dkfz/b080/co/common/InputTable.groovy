package de.dkfz.b080.co.common

import groovy.transform.CompileStatic
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

import java.util.Map.Entry

import static de.dkfz.b080.co.files.COConstants.*

/**
 * InputTable keeps an overview over all factors relevant for guiding the analyses, such as PID, SampleName / TissueType,
 * Library ID, FastQ file, Read Number. Plugins should subclass and override the getMandatoryColumns() and
 * getOptionalColumns() methods.
 */
@CompileStatic
class InputTable {

    private Map<String,Integer> headerMap
    private List<Map<String,String>> records

    public InputTable(Map<String,Integer> newHeaderMap, List<Map<String,String>> newTable) {
        records = newTable.collect { Map<String,String> record -> record.clone() } as List<Map<String,String>>;
        headerMap = newHeaderMap
    }

    public static InputTable readTable(Reader reader, String format) {
        CSVFormat tableFormat
        switch (format.toLowerCase()) {
            case "tsv":
                tableFormat = CSVFormat.TDF
                break
            case "excel":
                tableFormat = CSVFormat.EXCEL
                break
            case "csv":
                tableFormat = CSVFormat.RFC4180
                break
            default:
                throw new IllegalArgumentException("Value '${format}' is not a valid for ${CVALUE_INPUT_TABLE_FORMAT}. Use 'tsv', 'csv' or 'excel' (case-insensitive)!")
        }
        tableFormat = tableFormat.withCommentMarker('#' as char).withIgnoreEmptyLines().withHeader()
        CSVParser parser = tableFormat.parse(reader)
        return new InputTable(parser.headerMap as Map<String,Integer>, parser.records.collect { it.toMap() })

    }

    public static InputTable readTable(File file, String format) {
        Reader instream = new FileReader(file)
        InputTable inputTable = readTable(instream, format)
        instream.close()
        return inputTable
    }

    public static InputTable readTable(InputStream stream, String format) {
        return readTable(new InputStreamReader(stream), format)
    }

    public List<String> getMandatoryColumnNames() {
        return [INPUT_TABLE_DATASET,         // individual ID, cohort name, often pseudonym of patient
                INPUT_TABLE_SAMPLE_NAME,     // e.g. tumor, control
                INPUT_TABLE_LIBRARY,         // library identifier
                INPUT_TABLE_RUN_ID,          // e.g. run150626_ST-E00204_0045_AH5MK5CCXX
                INPUT_TABLE_READ_NUMBER,     // 1 or 2 representing read 1 or read 2
                INPUT_TABLE_FILE] as List<String>
    }

    public List<String> getOptionalColumnNames() {
        return [] as List<String>
    }

    public List<String> getRelevantColumnNames() {
        return mandatoryColumnNames + optionalColumnNames
    }

    private void assertValidRecord(Map<String,String> record) {
        if (record.keySet().equals(headerMap.keySet())) {
            throw new RuntimeException("Record has columns inconsistent with header: ${record}")
        }
        mandatoryColumnNames.each {
            if (!record.containsKey(it) && record.get(it) != "") {
                throw new RuntimeException("Field '${it}' is not set for record: ${record}")
            }
        }
    }

    private void assertUniqueFastq() {
        // ... what about BAMs?
        Map<String,Integer> tooFrequentFiles = records.countBy {
            it.get(INPUT_TABLE_FILE)
        }.findAll { file, count ->
            count > 1
        }
        if (tooFrequentFiles.size() > 0) {
            throw new RuntimeException("Files occur too often in input table: ${tooFrequentFiles}")
        }
    }

    private void assertHeader() {
        mandatoryColumnNames.each {
            if (!headerMap.containsKey(it)) {
                throw new RuntimeException("Field '${it}' is missing")
            }
        }
    }

    public void assertValidTable() {
        assertHeader()
        records.each { assertValidRecord(it) }
        assertUniqueFastq()
    }

    public Map<String,Integer> getHeaderMap() {
        return headerMap as Map<String,Integer>
    }

    /**
     * @return The header names in the order defined by the headerMap
     */
    public List<String> getHeader() {
        return headerMap.entrySet().collect { it.key } as List<String>
    }

    public List<Map<String,String>> getTable() {
        return records.collect { it.clone() } as List<Map<String,String>>
    }

    public InputTable subsetByColumn(String columnName, String value) {
        return new InputTable (headerMap, records.findAll { Map<String,String> row ->
            row.get(columnName) == value
        })
    }

    public InputTable subsetByDataset(String datasetId) {
        return subsetByColumn(INPUT_TABLE_DATASET, datasetId)
    }

    public InputTable subsetBySample(String sampleName) {
        return subsetByColumn(INPUT_TABLE_SAMPLE_NAME, sampleName)
    }

    public Integer size() {
        return records.size()
    }

    public List<String> listColumn(String columnName) {
        return records.collect { Map<String,String> record ->
            record.get(columnName)
        }
    }

    public List<String> listSampleNames() {
        return listColumn(INPUT_TABLE_SAMPLE_NAME).unique()
    }

    public List<String> listLibraries() {
        return listColumn(INPUT_TABLE_LIBRARY).unique()
    }

}
