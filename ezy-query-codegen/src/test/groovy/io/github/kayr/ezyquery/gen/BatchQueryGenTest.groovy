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


        def generatedFiles = BatchQueryGen.create(inputDir, outputDir).generateAndWrite()
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

    def 'test uses ezy-query properties file'(){
        def inputPathStr = BatchQueryGenTest.class.getResource("/generated/custom-java-types").toURI()


        when:
        //get package directory from classpath
        def inputDir = Paths.get(inputPathStr)
        println("inputDir: " + inputDir)

        //create a temporary directory
        def outputDir = Files.createDirectories(inputDir.resolveSibling("sql-files-out"))
        println("outputDir: " + outputDir)


        def generatedFiles = BatchQueryGen.create(inputDir, outputDir).generateAndWrite()

        def javaTypesFile = outputDir.resolve("JavaTypes.java")

        then:
        generatedFiles.size() == 1
        generatedFiles.contains(javaTypesFile)

        Files.exists(javaTypesFile)
        javaTypesFile.toFile().text.contains("Field.of(\"maq.f1\", \"f1\", BigDecimal.class,Field.ExpressionType.COLUMN);")
        javaTypesFile.toFile().text.contains("Field.of(\"maq.f2\", \"f2\", String.class,Field.ExpressionType.COLUMN);")
        javaTypesFile.toFile().text.contains("Field.of(\"maq.f3\", \"f3\", Vector.class,Field.ExpressionType.COLUMN);")
        javaTypesFile.toFile().text.contains("Field.of(\"maq.f4\", \"f4\", LocalDate.class,Field.ExpressionType.COLUMN);")
    }
}
