package io.github.kayr.ezyquery.gen

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

class TestUtil {
    static Tuple3<String, String, Properties> load(String path) {
        def sql = QueryGenTest.class.getResource("/generated/$path/in.sql.txt").text
        def java = QueryGenTest.class.getResource("/generated/$path/out.java.txt").text
        def pUrl = QueryGenTest.class.getResource("/generated/$path/ezy-query.properties")
        def properties = new Properties();
        if (pUrl != null) {
            pUrl.withInputStream {
                properties.load(it)
            }

        }
        return new Tuple3(sql, java, properties)
    }

    static void copyToClipboard(String s) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
        TimeUnit.SECONDS.sleep(5)
    }

    static overWriteFile(String path, String content) {
        def resourcesFolder = "src/test/resources"

        def java = Paths.get(resourcesFolder, "/generated/$path/out.java.txt")
        println("Overwriting file: ${java.toAbsolutePath()}")
        java.toFile().write(content)

    }
}
