package io.github.kayr.gradle.ezyquery;

import io.github.kayr.ezyquery.gen.BatchQueryGen;
import java.io.File;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;

public class EzyQueryBuildTask extends DefaultTask {
  private static final Logger logger = Logging.getLogger(EzyQueryBuildTask.class);

  private final EzyQueryPluginExtension extension;

  @Inject
  public EzyQueryBuildTask(EzyQueryPluginExtension extension) {
    this.extension = extension;
    setDescription("Generates EzyQuery classes");
  }

  @TaskAction
  public void generate() {
    logger.log(LogLevel.LIFECYCLE, "EzyQuery: Generating Query Classes");

    SourceSetContainer sourceSets = EzyQueryGradleHelper.getSourceSets(getProject());

    for (SourceSet sourceSet : sourceSets) {

      Optional<File> ezyQueryDir = findEzyQuerySourceDirectory(sourceSet);

      if (!ezyQueryDir.isPresent()) continue;

      if (!ezyQueryDir.get().exists()) continue;

      File directory = resolveOutputDirectory(sourceSet);

      EzyQueryFileUtils.createDirs(directory.toPath());

      logger.log(
          LogLevel.LIFECYCLE, "Generating Classes to: " + directory + " : From : " + ezyQueryDir);

      BatchQueryGen batchQueryGen =
          new BatchQueryGen(ezyQueryDir.get().toPath(), directory.toPath());

      batchQueryGen.generateAndWrite();
    }
  }

  private static Optional<File> findEzyQuerySourceDirectory(SourceSet sourceSet) {
    Set<File> srcDirs = sourceSet.getResources().getSrcDirs();
    return srcDirs.stream().filter(f -> f.getName().equals("ezyquery")).findFirst();
  }

  private File resolveOutputDirectory(SourceSet sourceSet) {
    File directory = extension.mainOutputDir();
    if (sourceSet.getName().equals("test")) {
      directory = extension.testOutputDir();
    }
    return directory;
  }
}
