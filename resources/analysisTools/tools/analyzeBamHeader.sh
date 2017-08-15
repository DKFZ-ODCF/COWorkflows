#!/bin/bash

# This script offers several functions to get data out of a bam file header.
# The script is supposed to run in roddy environments and needs a proper configuration set.

function getRefGenomeAndChrPrefixFromHeader {
    if [[ ${disableAutoBAMHeaderAnalysis-false} == false ]]
    then
        CHROMOSOME_LENGTH_FILE="${chromosomeLengthFile_hg19}"
    #    <cvalue name="CHROM_SIZES_FILE" value="${chromosomeSizesFile_hs37}" type="path" />

    #        <cvalue name='chromosomeSizesFile_mm10_GRC' value='${chromosomeSizesBaseDirectory_mm10}/GRCm38mm10.fa.chrLenOnlyACGT_realChromosomes.tab' type="path"/>
    #        <cvalue name='chromosomeSizesFile_mm10' value='${chromosomeSizesBaseDirectory_mm10}/mm10_1-19_X_Y_M.fa.chrLenOnlyACGT_realChromosomes.tab' type="path"/>
        countCHRPrefixes=`${SAMTOOLS_BINARY} view -H ${1} | grep "@SQ" | grep "SN:chr" | wc -l`
        if [[ $countCHRPrefixes -gt 0 ]]
        then
            CHR_PREFIX="chr"
            REFERENCE_GENOME=${referenceGenome_hg19_chr}
            CHROM_SIZES_FILE=${chromosomeSizesFile_hg19}
        else
            CHR_PREFIX=""
            REFERENCE_GENOME=${referenceGenome_1KGRef}
            CHROM_SIZES_FILE=${chromosomeSizesFile_hs37}
        fi
    fi
}
