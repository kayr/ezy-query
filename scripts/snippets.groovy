#!/usr/bin/env groovy
import java.nio.file.*

if (args.length < 2) {
    println "Usage: extractSnippets.groovy <sourceDir> <markdownFile>"
    System.exit(1)
}

def javaSrcDir = Paths.get(args[0])
def markdownFile = Paths.get(args[1])

if (!Files.exists(javaSrcDir) || !Files.isDirectory(javaSrcDir)) {
    println "Source dir '${javaSrcDir}' does not exist or is not a directory."
    System.exit(1)
}
if (!Files.exists(markdownFile) || Files.isDirectory(markdownFile)) {
    println "Markdown file '${markdownFile}' does not exist or is a directory."
    System.exit(1)
}

def snippets = [:]
def snippetName = null
def snippetLines = []

// Extract snippets from Java
Files.walk(javaSrcDir)
        .filter { it.toString().endsWith(".java") || it.toString().endsWith(".groovy") }
        .forEach { file ->
            file.eachLine { line ->
                def startMatch = line =~ /\/\/\s*snippet:(\S+)/
                def endMatch = line =~ /\/\/\s*endsnippet/

                if (startMatch) {
                    snippetName = startMatch[0][1]
                    snippetLines = []
                } else if (endMatch) {
                    snippets[snippetName] = snippetLines.join("\n")
                    snippetName = null
                } else if (snippetName && !line.trim().endsWith("//nosnippet")) {
                    snippetLines << line
                }
            }
        }

println("Found snippets")
snippets.each {
    println(it.key)
}

// Update Markdown with indentation-aware snippet insertion
def updatedLines = []
def inSnippet = false
//def currentName = null

markdownFile.eachLine { line ->
    def startMatch = line =~ /^(\s*)<!--\s*snippet:(\S+)\s*-->/
    def endMatch = line =~ /^(\s*)<!--\s*endsnippet\s*-->/

    if (startMatch) {
        def indent = startMatch[0][1]  // capture indentation whitespace
        def currentName = startMatch[0][2]

        updatedLines << line
        if (snippets.containsKey(currentName)) {
            updatedLines << indent + "```java"
            snippets[currentName].split("\n").each { snippetLine ->
                updatedLines << indent + snippetLine
            }
            updatedLines << indent + "```"
        } else {
            updatedLines << indent + "<<missing snippet: ${currentName}>>"
        }
        inSnippet = true
    } else if (endMatch) {
        inSnippet = false
        updatedLines << line
    } else if (!inSnippet) {
        updatedLines << line
    }
}

markdownFile.text = updatedLines.join("\n")
println "✅ Updated $markdownFile with ${snippets.size()} snippet(s)."
