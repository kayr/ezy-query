package io.github.kayr.ezyquery.util

import spock.lang.Specification

import java.nio.file.Paths

class ElfTest extends Specification {
    def "ResolveOutputPath"() {

        expect:
        Elf.resolveOutputPath(parentIn, file, parentOut) == expected
        where:
        parentIn | file              | parentOut | expected
        "in"     | "in/file"         | "out"     | "out/file"
        "in"     | "in/file/"        | "out/"    | "out/file"
        "in"     | "in/d1/d2/d3"     | "out/"    | "out/d1/d2/d3"
        "in"     | "in/d1/d2/d3.sql" | "out/"    | "out/d1/d2/d3.sql"

    }

    def 'test file has to be child of parent path'() {
        when:
        Elf.resolveOutputPath("in", "blah/file", "out")

        then:
        thrown(IllegalStateException)
    }

    def 'test we can change the extension'() {
        expect:
        def filePath = Paths.get(file)
        def newFilePath = Paths.get(newFile)
        Elf.changeExtension(filePath, extension) == newFilePath
        where:
        file                        | extension | newFile
        "file.sql"                  | "txt"     | "file.txt"
        "file"                      | "txt"     | "file.txt"
        "file.sql"                  | "java"    | "file.java"
        "dot.before.file.sql"       | "java"    | "dot.before.file.java"
        "/path/dot.before.file.sql" | "java"    | "/path/dot.before.file.java"

    }

    def 'test adding to array'() {
        expect:
        ArrayElf.addAll(array, value) == expected

        where:
        array                | value                | expected
        null                 | 1                    | ArrayElf.array(1)
        ArrayElf.array(1, 2) | ArrayElf.array(2, 4) | ArrayElf.array(1, 2, 2, 4)
        ArrayElf.array(1, 2) | 3                    | ArrayElf.array(1, 2, 3)
        ArrayElf.array(1, 2) | null                 | ArrayElf.array(1, 2)
        null                 | null                 | null

    }
}
