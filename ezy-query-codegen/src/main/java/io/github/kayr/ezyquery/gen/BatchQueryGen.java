package io.github.kayr.ezyquery.gen;

import io.github.kayr.ezyquery.util.Elf;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.java.Log;

@Log
public class BatchQueryGen {

  private final Path inputPath;
  private final Path outputPath;
  private final Properties config;

  private BatchQueryGen(Path inputPath, Path outputPath, Properties config) {
    this.inputPath = inputPath;
    this.outputPath = outputPath;
    this.config = config;
    Elf.assertTrue(Files.isDirectory(inputPath), "Input path must be a directory");
    Elf.assertTrue(Files.isDirectory(outputPath), "Output path must be a directory");
  }

  public static BatchQueryGen create(Path inputPath, Path outputPath) {
    Properties prop = mayBeLoadConfig(inputPath);
    return create(inputPath, outputPath, prop);
  }

  public static BatchQueryGen create(Path inputPath, Path outputPath, Properties config) {
    return new BatchQueryGen(inputPath, outputPath, config);
  }

  private static Properties mayBeLoadConfig(Path inputPath) {
    Path propFile = inputPath.resolve("ezy-query.properties");
    Properties prop = new Properties();
    if (Files.exists(propFile)) {
      log.info("Reading properties from: " + propFile);
      prop = Elf.readProperties(propFile);
    }
    return prop;
  }

  public List<Path> generateAndWrite() {
    return readAllCode().map(this::generate).map(this::writeJavaFile).collect(Collectors.toList());
  }

  private Path writeJavaFile(WritesCode gen) {
    Path path = gen.writeTo(outputPath);
    log.info("Writing file: " + path);
    return path;
  }

  private Stream<SourceCode> readAllCode() {
    return Elf.listAllSqlFiles(inputPath).stream().map(BatchQueryGen::readCode);
  }

  private static SourceCode readCode(Path path) {
    String code = Elf.readText(path);
    return new SourceCode(code, path);
  }

  private WritesCode generate(SourceCode code) {
    String packageName = resolvePackageName(inputPath, code.path);
    String withNoSqlExt = removeExtension(code.path.getFileName().toString());

    WritesCode writesCode;
    if (withNoSqlExt.endsWith(".ez")) {
      String className = Elf.fromKebabToCamelCase(removeExtension(withNoSqlExt));
      writesCode = EzySqlQueryGen.of(packageName, className, code.code, config);
    } else if (withNoSqlExt.endsWith(".static")) {
      String className = Elf.fromKebabToCamelCase(removeExtension(withNoSqlExt));
      writesCode = StaticQueryGen.of(packageName, className, code.code);
    } else {
      String className = Elf.fromKebabToCamelCase(withNoSqlExt);
      writesCode = new QueryGen(packageName, className, code.code, config);
    }

    return path -> {
      try {
        return writesCode.writeTo(path);
      } catch (Exception x) {
        throw new CodeGenException(
            "Failed to generate code for: " + code.path + ". Did forget to mark it as .static.sql",
            x);
      }
    };
  }

  String removeExtension(String fileName) {
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex == -1) {
      return fileName;
    }
    return fileName.substring(0, dotIndex);
  }

  private String resolvePackageName(Path parentPath, Path filePath) {
    Path relativePath = parentPath.relativize(filePath.getParent());
    return relativePath.toString().replace("/", ".");
  }

  @lombok.Getter
  @lombok.AllArgsConstructor
  public static class SourceCode {
    private String code;
    private Path path;
  }
}
