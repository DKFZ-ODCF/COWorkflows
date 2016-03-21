package de.dkfz.b080.co.common

import de.dkfz.roddy.Roddy
import de.dkfz.roddy.plugins.LibrariesFactory
import groovy.transform.CompileStatic;
import org.junit.Test;

import static org.junit.Assert.*
import org.hamcrest.core.*

@CompileStatic
public class InputTableTest {

    public File resourceDir =  new File("test/resources");

    @Test
    public void testReadTable_correctTable() throws Exception {
        String testFileName = LibrariesFactory.groovyClassLoader.getResource("InputTableTest_CorrectTable1.tsv").file
        FileReader testFile = new FileReader(testFileName)
        InputTable inputTable = InputTable.readTable(testFile, "tsv")
    }

    @Test
    public void testAssertValidTable() throws Exception {
        fail()
    }

    @Test
    public void testGetHeader() throws Exception {
        fail()
    }

    @Test
    public void testSubsetByColumn() throws Exception {
        fail()
    }

    @Test
    public void testListSampleNames() throws Exception {
        fail()
    }
}