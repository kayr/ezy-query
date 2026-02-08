package io.github.kayr.gradle.ezyquery;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;

public class EzyQueryInitTask extends DefaultTask {
  private static final Logger logger = Logging.getLogger(EzyQueryInitTask.class);

  @Inject
  public EzyQueryInitTask() {
    setDescription("Generates Query Source Directories");
  }

  @TaskAction
  public void generate() throws IOException {
    logger.log(LogLevel.LIFECYCLE, "EzyQuery: Generating Query Source Directories");

    SourceSetContainer sourceSets = EzyQueryGradleHelper.getSourceSets(getProject());

    for (SourceSet sourceSet : sourceSets) {
      List<File> ezyQueryDirs =
          EzyQueryGradleHelper.resolveEzyQueryDirectories(getProject(), sourceSet, true);

      for (File ezyQueryDir : ezyQueryDirs) {
        if (!ezyQueryDir.exists()) {
          logger.log(LogLevel.LIFECYCLE, "Creating ezyquery dir: " + ezyQueryDir);
          Files.createDirectories(ezyQueryDir.toPath());
        }
      }
    }
  }
}
