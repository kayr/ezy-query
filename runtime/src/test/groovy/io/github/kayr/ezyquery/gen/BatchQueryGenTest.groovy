package io.github.kayr.ezyquery.gen

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class BatchQueryGenTest extends Specification {
    def "GenerateAndWrite"() {

        def inputPathStr = BatchQueryGenTest.class.getResource("/sql-files").toURI()
        when:

        //get package directory from classpath
        def inputDir = Paths.get(inputPathStr)
        println("inputDir: " + inputDir)

        //create a temporary directory
        def outputDir = Files.createDirectories(inputDir.resolveSibling("sql-files-out"))
        println("outputDir: " + outputDir)

        //generate and write
        def write = new BatchQueryGen(inputDir, outputDir).generateAndWrite()




        then:
        1 == 1

    }
}
