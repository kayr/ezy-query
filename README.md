# ezy-query

Convert Your Sql Query To A Queryable Java API/Code.. think of A Queryable View In Your Code Using Java

Adding to your gradle build

```groovy
plugins {
    id 'io.github.kayr.gradle.ezyquery' version '0.0.5'
}
```

This lib is still under experimentation so the gradle plugin may not work on all versions.

If this is the case then you can manually run the tasks in your gradle build file.

```groovy

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "io.github.kayr:ezy-query-codegen:0.0.2"
    }
}

task("ezyBuild") {

    def input = file("src/main/ezyquery").toPath()
    def output = file("build/generated/ezy/main").toPath()

    doLast {
        if (input.toFile().exists()) {
            Files.createDirectories(output)
            BatchQueryGen.generate(input, output)
        }
    }
}
task("ezyClean") {
    doLast {
        project.delete("build/generated/ezy/")
    }
}

sourceSets {
    main {
        java {
            srcDir "build/generated/ezy/main"
        }

    }
    test {
        java {
            srcDir "build/generated/ezy/test"
        }
    }
}
```

You can then execute the tasks:

```bash
   ./gradlew ezyBuild
```

or

```bash
    ./gradlew ezyClean
```

