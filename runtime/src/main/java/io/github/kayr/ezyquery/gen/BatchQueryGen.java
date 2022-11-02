package io.github.kayr.ezyquery.gen;

import io.github.kayr.ezyquery.util.Elf;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BatchQueryGen {

  private final Path inputPath;
  private final Path outputPath;

  public BatchQueryGen(Path inputPath, Path outputPath) {
    this.inputPath = inputPath;
    this.outputPath = outputPath;
    Elf.assertTrue(Files.isDirectory(inputPath), "Input path must be a directory");
    Elf.assertTrue(Files.isDirectory(outputPath), "Output path must be a directory");
  }

  public List<Path> generateAndWrite() {
    return readAllCode()
        .map(this::generate)
        .map(this::writeToJavaFile)
        .collect(Collectors.toList());
  }

  private Path writeToJavaFile(QueryGen gen) {

    gen.writeTo(outputPath);
    return outputPath;
  }

  private Stream<SourceCode> readAllCode() {
    return Elf.listAllSqlFiles(inputPath).stream().map(BatchQueryGen::readCode);
  }

  private static SourceCode readCode(Path path) {
    String code = Elf.readText(path);
    return new SourceCode(code, path);
  }

  private QueryGen generate(SourceCode code) {
    String packageName = resolvePackageName(inputPath, code.path);
    String className =
        Elf.fromKebabToCamelCase(code.path.getFileName().toString().replace(".sql", ""));
    String sql = code.code;
    return new QueryGen(packageName, className, sql);
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
