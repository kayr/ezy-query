package io.github.kayr.gradle.ezyquery;

import io.github.kayr.ezyquery.gen.BatchQueryGen;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.*;

public class EzyQueryBuildTask extends DefaultTask {
  private static final Logger logger = Logging.getLogger(EzyQueryBuildTask.class);

  private final EzyQueryPluginExtension extension;
  private final DirectoryProperty testOutputDir;
  private final DirectoryProperty testInputDir;

  private final DirectoryProperty mainOutputDir;
  private final DirectoryProperty mainInputDir;

  @Inject
  public EzyQueryBuildTask(EzyQueryPluginExtension extension) {
    this.extension = extension;
    testOutputDir = getProject().getObjects().directoryProperty();
    mainOutputDir = getProject().getObjects().directoryProperty();

    testInputDir = getProject().getObjects().directoryProperty();
    mainInputDir = getProject().getObjects().directoryProperty();

    setInputAndOutputDir();
    setDescription("Generates EzyQuery classes");
  }

  private void setInputAndOutputDir() {

    testOutputDir.set(extension.testOutputDir());
    mainOutputDir.set(extension.mainOutputDir());

    SourceSetContainer sourceSets = EzyQueryGradleHelper.getSourceSets(getProject());
    for (SourceSet sourceSet : sourceSets) {
      mayBeSetEzyQueryDir(sourceSet);
    }
  }

  private void mayBeSetEzyQueryDir(SourceSet sourceSet) {
    Optional<File> ezyQueryDir = EzyQueryGradleHelper.findEzyQuerySourceDirectory(sourceSet);

    //noinspection SimplifyOptionalCallChains
    if (!ezyQueryDir.isPresent()) return;

    File theInputDir = ezyQueryDir.get();

    if (!theInputDir.exists()) return;

    String sourceSetName = sourceSet.getName();

    switch (sourceSetName) {
      case SourceSet.MAIN_SOURCE_SET_NAME:
        mainInputDir.set(theInputDir);
        mainOutputDir.set(extension.mainOutputDir());
        break;
      case SourceSet.TEST_SOURCE_SET_NAME:
        testInputDir.set(theInputDir);
        testOutputDir.set(extension.testOutputDir());
        break;
    }
  }

  @TaskAction
  public void generate() {
    logger.log(LogLevel.LIFECYCLE, "EzyQuery: Generating Query Classes");
    // generate for main files
    generate(mainInputDir, mainOutputDir);
    generate(testInputDir, testOutputDir);
  }

  private void generate(DirectoryProperty input, DirectoryProperty output) {
    if (!input.isPresent() || !input.get().getAsFile().exists()) return;

    Path outPath = output.get().getAsFile().toPath();
    Path inputPath = input.get().getAsFile().toPath();

    EzyQueryFileUtils.createDirs(outPath);
    logger.log(LogLevel.LIFECYCLE, "Generating Classes to: " + outPath + " : From : " + inputPath);

    BatchQueryGen.create(inputPath, outPath).generateAndWrite();
  }

  @InputDirectory
  @org.gradle.api.tasks.Optional
  public DirectoryProperty getMainInputDir() {
    return mainInputDir;
  }

  @OutputDirectory
  public DirectoryProperty getMainOutputDir() {
    return mainOutputDir;
  }

  @InputDirectory
  @org.gradle.api.tasks.Optional
  public DirectoryProperty getTestInputDir() {
    return testInputDir;
  }

  @OutputDirectory
  public DirectoryProperty getTestOutputDir() {
    return testOutputDir;
  }
}
