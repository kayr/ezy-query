package io.github.kayr.ezyquery.gen

import spock.lang.Specification

import java.nio.file.Files
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


        def generatedFiles = BatchQueryGen.generate(inputDir, outputDir)
        def departmentsPath = outputDir.resolve("office/SelectDepartments.java")
        def employeesPath = outputDir.resolve("office/SelectEmployees.java")
        def customersPath = outputDir.resolve("SelectCustomers.java")

        then:

        generatedFiles.size() == 3
        generatedFiles.containsAll([departmentsPath, employeesPath, customersPath])


        Files.exists(departmentsPath)
        Files.exists(employeesPath)
        Files.exists(customersPath)

        departmentsPath.toFile().text.contains("package office;")
        employeesPath.toFile().text.contains("package office;")
        !customersPath.toFile().text.contains("package")

    }
}
