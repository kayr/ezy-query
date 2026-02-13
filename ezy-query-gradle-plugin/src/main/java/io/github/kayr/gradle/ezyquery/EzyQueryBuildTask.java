package io.github.kayr.gradle.ezyquery;

import io.github.kayr.ezyquery.gen.BatchQueryGen;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.*;

public class EzyQueryBuildTask extends DefaultTask {
  private static final Logger logger = Logging.getLogger(EzyQueryBuildTask.class);

  private final EzyQueryPluginExtension extension;
  private final DirectoryProperty testOutputDir;
  private final DirectoryProperty mainOutputDir;

  private final ConfigurableFileCollection mainInputDirs;
  private final ConfigurableFileCollection testInputDirs;

  @Inject
  public EzyQueryBuildTask(EzyQueryPluginExtension extension) {
    this.extension = extension;
    testOutputDir = getProject().getObjects().directoryProperty();
    mainOutputDir = getProject().getObjects().directoryProperty();

    mainInputDirs = getProject().getObjects().fileCollection();
    testInputDirs = getProject().getObjects().fileCollection();

    setInputAndOutputDir();
    setDescription("Generates EzyQuery classes");
  }

  private void setInputAndOutputDir() {
    testOutputDir.set(extension.testOutputDir());
    mainOutputDir.set(extension.mainOutputDir());

    SourceSetContainer sourceSets = EzyQueryGradleHelper.getSourceSets(getProject());
    for (SourceSet sourceSet : sourceSets) {
      collectEzyQueryDirs(sourceSet);
    }
  }

  private void collectEzyQueryDirs(SourceSet sourceSet) {
    List<File> ezyQueryDirs = EzyQueryGradleHelper.findAllEzyQuerySourceDirectories(sourceSet);

    for (File dir : ezyQueryDirs) {
      if (!dir.exists()) continue;

      switch (sourceSet.getName()) {
        case SourceSet.MAIN_SOURCE_SET_NAME:
          mainInputDirs.from(dir);
          break;
        case SourceSet.TEST_SOURCE_SET_NAME:
          testInputDirs.from(dir);
          break;
      }
    }
  }

  @TaskAction
  public void generate() {
    logger.log(LogLevel.LIFECYCLE, "EzyQuery: Generating Query Classes");
    generateAll(mainInputDirs.getFiles(), mainOutputDir);
    generateAll(testInputDirs.getFiles(), testOutputDir);
  }

  private void generateAll(Set<File> inputDirs, DirectoryProperty output) {
    for (File inputDir : inputDirs) {
      if (!inputDir.exists()) continue;

      Path outPath = output.get().getAsFile().toPath();
      Path inputPath = inputDir.toPath();

      EzyQueryFileUtils.createDirs(outPath);
      logger.log(
          LogLevel.LIFECYCLE, "Generating Classes to: " + outPath + " : From : " + inputPath);

      BatchQueryGen.create(inputPath, outPath).generateAndWrite();
    }
  }

  @InputFiles
  @SkipWhenEmpty
  @org.gradle.api.tasks.Optional
  public ConfigurableFileCollection getMainInputDirs() {
    return mainInputDirs;
  }

  @OutputDirectory
  public DirectoryProperty getMainOutputDir() {
    return mainOutputDir;
  }

  @InputFiles
  @SkipWhenEmpty
  @org.gradle.api.tasks.Optional
  public ConfigurableFileCollection getTestInputDirs() {
    return testInputDirs;
  }

  @OutputDirectory
  public DirectoryProperty getTestOutputDir() {
    return testOutputDir;
  }
}
