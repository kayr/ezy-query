package io.github.kayr.gradle.ezyquery;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
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
      List<File> ezyQueryDirs = resolveEzyQueryDirs(sourceSet);

      for (File ezyQueryDir : ezyQueryDirs) {
        if (!ezyQueryDir.exists()) {
          logger.log(LogLevel.LIFECYCLE, "Creating ezyquery dir: " + ezyQueryDir);
          Files.createDirectories(ezyQueryDir.toPath());
        }
      }
    }
  }

  /**
   * Resolve ezyquery directories for a source set. Creates an {@code ezyquery} sibling directory
   * next to each java srcDir. For example, if the source set has java srcDirs {@code
   * src/core/main/java} and {@code src/savings/main/java}, this returns {@code
   * src/core/main/ezyquery} and {@code src/savings/main/ezyquery}.
   *
   * <p>Falls back to the conventional location ({@code src/{sourceSetName}/ezyquery}) if there are
   * no java srcDirs with a parent directory.
   */
  private List<File> resolveEzyQueryDirs(SourceSet sourceSet) {
    File buildDir = getProject().getLayout().getBuildDirectory().getAsFile().get();
    List<File> dirs = new ArrayList<>();
    for (File javaSrcDir : sourceSet.getJava().getSrcDirs()) {
      if (javaSrcDir.getParentFile() == null) continue;
      // Skip generated source dirs (under the build directory)
      if (isChildOf(javaSrcDir, buildDir)) continue;
      dirs.add(new File(javaSrcDir.getParentFile(), "ezyquery"));
    }
    if (!dirs.isEmpty()) {
      return dirs;
    }

    // Fallback: conventional location
    return List.of(getProject().file("src/" + sourceSet.getName() + "/ezyquery"));
  }

  private static boolean isChildOf(File child, File parent) {
    return child.toPath().startsWith(parent.toPath());
  }
}
