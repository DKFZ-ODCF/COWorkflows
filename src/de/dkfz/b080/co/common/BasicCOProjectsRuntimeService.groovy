/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package de.dkfz.b080.co.common

import de.dkfz.b080.co.files.*
import de.dkfz.roddy.StringConstants
import de.dkfz.roddy.config.Configuration
import de.dkfz.roddy.core.*
import de.dkfz.roddy.execution.io.fs.FileSystemAccessProvider
import de.dkfz.roddy.execution.jobs.CommandFactory
import de.dkfz.roddy.knowledge.files.BaseFile
import de.dkfz.roddy.tools.LoggerWrapper

//import net.xeoh.plugins.base.annotations.PluginImplementation

/**
 * This service is mainly for qcpipeline. It might be of use for other projects
 * as well. TODO Think about renaming and extending this as needed.
 *
 * @author michael
 */
//@PluginImplementation
@groovy.transform.CompileStatic
public class BasicCOProjectsRuntimeService extends RuntimeService {

    private static LoggerWrapper logger = LoggerWrapper.getLogger(BasicCOProjectsRuntimeService.class.getName());

    private static List<File> alreadySearchedMergedBamFolders = [];

    private static Map<ExecutionContext,InputTable> inputTablesByContext

    /**
     * Releases the cache in this provider
     */
    @Override
    public void releaseCache() {

    }

    @Override
    public boolean initialize() {
    }

    @Override
    public void destroy() {
    }

    public Map<String, Object> getDefaultJobParameters(ExecutionContext context, String toolID) {
        def fs = context.getRuntimeService();
        //File cf = fs..createTemporaryConfigurationFile(executionContext);
        String pid = context.getDataSet().toString()
        Map<String, Object> parameters = [
                pid         : (Object) pid,
                PID         : pid,
                CONFIG_FILE : fs.getNameOfConfigurationFile(context).getAbsolutePath(),
                ANALYSIS_DIR: context.getOutputDirectory().getParentFile().getParent()
        ]
        return parameters;
    }

    @Override
    public String createJobName(ExecutionContext executionContext, BaseFile file, String toolID, boolean reduceLevel) {
        return CommandFactory.getInstance().createJobName(file, toolID, reduceLevel);
    }

    /**
     * Checks if a folder is valid
     *
     * A folder is valid if:
     * <ul>
     *   <li>its parents are valid</li>
     *   <li>it was not created recently (within this context)</li>
     *   <li>it exists</li>
     *   <li>it can be validated (i.e. by its size or files, but not with a lengthy operation!)</li>
     * </ul>
     */
    @Override
    public boolean isFileValid(BaseFile baseFile) {
        //Parents valid?
        boolean parentsValid = true;
        for (BaseFile bf in baseFile.parentFiles) {
            if (bf.isTemporaryFile()) continue; //We do not check the existence of parent files which are temporary.
            if (bf.isSourceFile()) continue;
            if (!bf.isFileValid()) {
                return false;
            }
        }

        boolean result = true;

        //Source files should be marked as such and checked in a different way. They are assumed to be valid.
        if (baseFile.isSourceFile())
            return true;

        //Temporary files are also considered as valid.
        if (baseFile.isTemporaryFile())
            return true;

        try {
            //Was freshly created?
            if (baseFile.creatingJobsResult != null && baseFile.creatingJobsResult.wasExecuted) {
                result = false;
            }
        } catch (Exception ex) {
            result = false;
        }

        try {
            //Does it exist and is it readable?
            if (result && !baseFile.isFileReadable()) {
                result = false;
            }
        } catch (Exception ex) {
            result = false;
        }

        try {
            //Can it be validated?
            //TODO basefiles are always validated!
            if (result && !baseFile.checkFileValidity()) {
                result = false;
            }
        } catch (Exception ex) {
            result = false;
        }

        // TODO? If the file is not valid then also temporary parent files should be invalidated! Or at least checked.
        if (!result) {
            // Something is missing here! Michael?
        }

        return result;
    }


    public InputTable getInputTable(ExecutionContext context) {
        if (!inputTablesByContext.containsKey(context)) {
            COConfig cfg = new COConfig(context);
            InputTable inputTable = InputTable.readTable(new File(cfg.inputTableFile), cfg.inputTableFormat)
            inputTable.assertValidTable()
            inputTablesByContext[context] = inputTable
        }
        return inputTablesByContext[context]
    }

    public InputTable inputTableForDataset(ExecutionContext context) {
        COConfig cfg = new COConfig(context);
        InputTable resultTable = getInputTable(context).subsetByDataset(context.dataSet.id)
        assert resultTable.size() > 0
        return resultTable
    }

    public List<Sample> extractSamplesFromInputTable(ExecutionContext context) {
        return inputTableForDataset(context).listSampleNames().collect {
            new Sample(context, it)
        }
    }

    public List<String> extractLibrariesFromInputTable(ExecutionContext context, String sampleName) {
        InputTable resultTable = inputTableForDataset(context).subsetBySample(sampleName)
        assert resultTable.size() > 0
        return resultTable.listLibraries()
    }

    public List<Sample> extractSamplesFromFastqList (ExecutionContext context) {
        COConfig cfg = new COConfig(context);
        List<String> fastqFiles = cfg.fastqFiles
        int indexOfSampleID = cfg.sequenceDirectory.split(StringConstants.SPLIT_SLASH).findIndexOf { it -> it == '${sample}' }
        return fastqFiles.collect {
            it.split(StringConstants.SPLIT_SLASH)[indexOfSampleID]
        }.unique().collect {
            new Sample(context, it)
        }
    }

    public static String extractSampleNameFromOutputFile(String filename, boolean enforceAtomicSampleName) {
        String[] split = filename.split(StringConstants.SPLIT_UNDERSCORE);
        if (split.size() <= 2) {
            return null
        }
        String sampleName = split[0];
        if (!enforceAtomicSampleName && split[1].isInteger() && split[1].length() <= 2)
            sampleName = split[0..1].join(StringConstants.UNDERSCORE);
        return sampleName
    }

    public List<Sample> extractSamplesFromOutputFiles(ExecutionContext context) {
        //TODO extractSamplesFromOutputFiles fails, when no alignment directory is available. Should one fall back to the default method?
        COConfig cfg = new COConfig(context);
        FileSystemAccessProvider fileSystemAccessProvider = FileSystemAccessProvider.getInstance()

        File alignmentDirectory = getAlignmentDirectory(context)
        if (!fileSystemAccessProvider.checkDirectory(alignmentDirectory, context, false)) {
            logger.severe("Cannot retrieve samples from missing directory: " + alignmentDirectory.absolutePath);
            return (List<Sample>) null;
        }
        List<File> filesInDirectory = fileSystemAccessProvider.listFilesInDirectory(alignmentDirectory).sort();

        return filesInDirectory.collect { File file ->
            extractSampleNameFromOutputFile(file.name, cfg.enforceAtomicSampleName)
        }.unique().collect {
            new Sample(context, it)
        }
    }

    public List<Sample> extractSamplesFromSampleDirs(ExecutionContext context) {
        FileSystemAccessProvider fileSystemAccessProvider = FileSystemAccessProvider.getInstance()

        if (!fileSystemAccessProvider.checkDirectory(context.inputDirectory, context, false)) {
            logger.severe("Cannot retrieve samples from missing directory: " + context.inputDirectory.absolutePath);
            return (List<Sample>) null;
        }
        List<File> sampleDirs = fileSystemAccessProvider.listFilesInDirectory(context.inputDirectory).sort();

        return sampleDirs.collect {
            new Sample(context, it)
        }
    }

    public List<String> extractLibrariesFromSampleDirectory(File sampleDirectory) {
        return FileSystemAccessProvider.getInstance().listDirectoriesInDirectory(sampleDirectory).collect { File f -> f.name } as List<String>;
    }

    public List<Sample> getSamplesForContext(ExecutionContext context) {
        COConfig cfg = new COConfig(context);
        List<Sample> samples
        String extractedFrom
        if (cfg.extractSamplesFromInputTable) {
            samples = extractSamplesFromInputTable(context)
            extractedFrom = "input table '${cfg.inputTableFile}'"
        } else if (cfg.extractSamplesFromFastqList) {
            samples = extractSamplesFromFastqList(context)
            extractedFrom = "fastq_list configuration value"
        } else if (cfg.extractSamplesFromOutputFiles) {
            samples = extractSamplesFromOutputFiles(context)
            extractedFrom = "output files"
        } else {
            samples = extractSamplesFromSampleDirs(context)
            extractedFrom = "subdirectories of input directory '${context.inputDirectory}'"
        }
        samples.removeAll { Sample sample ->
            sample.sampleType != Sample.SampleType.UNKNOWN
        }
        if (samples.size() == 0) {
            logger.warning("No valid samples could be extracted from ${extractedFrom} for dataset ${context.getDataSet().getId()}.")
        }
        return samples
    }

    protected File getAlignmentDirectory(ExecutionContext context) {
        COConfig cfg = new COConfig(context)
        return getDirectory(cfg.alignmentFolderName, context);
    }

    protected File getInpDirectory(String dir, ExecutionContext process, Sample sample, String library = null) {
        Configuration cfg = process.getConfiguration();
        File path = cfg.getConfigurationValues().get(dir).toFile(process);
        String temp = path.getAbsolutePath();
        temp = temp.replace('${dataSet}', process.getDataSet().toString());
        temp = temp.replace('${sample}', sample.getName());
        if(library)
            temp = temp.replace('${library}', library);
        else
            temp = temp.replace('${library}/', "");

        return new File(temp);
    }

    public File getSampleDirectory(ExecutionContext process, Sample sample, String library = null) {
        File sampleDir = getInpDirectory(COConstants.CVALUE_SAMPLE_DIRECTORY, process, sample, library);
        return sampleDir
    }

    public File getSequenceDirectory(ExecutionContext process, Sample sample, String run, String library = null) {
        return new File(getInpDirectory(COConstants.CVALUE_SEQUENCE_DIRECTORY, process, sample, library).getAbsolutePath().replace('${run}', run));
    }

    public BasicBamFile getMergedBamFileForDataSetAndSample(ExecutionContext context, Sample sample) {
        //TODO Create constants
        COConfig cfg = new COConfig(context)

        List<String> filters = [];
        for (String suffix in cfg.mergedBamSuffixList) {
            if (!cfg.searchMergedBamFilesWithPID) {
                filters += ["${sample.getName()}*${suffix}".toString()
                            , "${sample.getName().toLowerCase()}*${suffix}".toString()
                            , "${sample.getName().toUpperCase()}*${suffix}".toString()]
            } else {
                def dataSetID = context.getDataSet().getId()
                filters += ["${sample.getName()}*${dataSetID}*${suffix}".toString()
                            , "${sample.getName().toLowerCase()}*${dataSetID}*${suffix}".toString()
                            , "${sample.getName().toUpperCase()}*${dataSetID}*${suffix}".toString()]
            }
        }


        List<File> mergedBamPaths;

        File searchDirectory = getAlignmentDirectory(context);
        if (cfg.useMergedBamsFromInputDirectory)
            searchDirectory = getInpDirectory(COConstants.CVALUE_ALIGNMENT_INPUT_DIRECTORY_NAME, context, sample);

        synchronized (alreadySearchedMergedBamFolders) {
            if (!alreadySearchedMergedBamFolders.contains(searchDirectory)) {
                logger.postAlwaysInfo("Looking for merged bam files in directory ${searchDirectory.getAbsolutePath()}");
                alreadySearchedMergedBamFolders << searchDirectory;
            }
        }

        mergedBamPaths = FileSystemAccessProvider.getInstance().listFilesInDirectory(searchDirectory, filters);

        List<BasicBamFile> bamFiles = mergedBamPaths.collect({
            File f ->
                String name = f.getName();
                String[] split = name.split(StringConstants.SPLIT_UNDERSCORE);
                int runIndex = 1;
                if (split[1].isInteger()) {
                    runIndex = 2;
                }
                String run = split[runIndex..-2].join(StringConstants.UNDERSCORE);

//                                    BaseFile.ConstructionHelperForSourceFiles.construct(BasicBamFile, f, context, new COFileStageSettings(run, sample, context.getDataSet()));
                BasicBamFile bamFile = new BasicBamFile(new BaseFile.ConstructionHelperForSourceFiles(f, context, new COFileStageSettings(run, sample, context.getDataSet()), null));
                return bamFile;
        })

        if (bamFiles.size() == 1)
            logger.info("\tFound merged bam file ${bamFiles[0].getAbsolutePath()} for sample ${sample.getName()}");
        if (bamFiles.size() > 1) {
            StringBuilder info = new StringBuilder();
            info << "Found more ${bamFiles.size()} merged bam files for sample ${sample.getName()}.\nConsider using option searchMergedBamFilesWithPID=true in your configuration.";
            bamFiles.each { BasicBamFile bamFile -> info << "\t" << bamFile.getAbsolutePath() << "\n"; }

            logger.postAlwaysInfo(info.toString());
            return null;
        }
        if (bamFiles.size() == 0) {
            logger.severe("Found no merged bam file for sample ${sample.getName()}. Please make sure that merged bam files exist or are linked to the alignment folder within the result folder.");
            return null;
        }

        return bamFiles[0];
    }



}
