package io.github.kayr.gradle.ezyquery;

import io.github.kayr.ezyquery.EzyQueryVersion;
import java.io.File;
import java.util.Optional;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;

public class EzyQueryPlugin implements Plugin<Project> {

  private static Logger logger = Logging.getLogger(EzyQueryPlugin.class);

  @Override
  public void apply(Project project) {
    // also apply the java plugin
    project.getPlugins().apply(JavaBasePlugin.class);
    project.getPluginManager().withPlugin("java", p -> doApply(project));
  }

  private void doApply(Project project) {
    // add ezyquery dependency version
    project
        .getDependencies()
        .add("implementation", "io.github.kayr:ezy-query-core:" + EzyQueryVersion.VERSION);

    // add project extension
    EzyQueryPluginExtension extension =
        project.getExtensions().create("ezyQuery", EzyQueryPluginExtension.class);

    TaskProvider<EzyQueryBuildTask> ezyBuild =
        project.getTasks().register("ezyBuild", EzyQueryBuildTask.class, extension);

    project.getTasks().register("ezyClean", EzyQueryCleanTask.class, extension);

    project.getTasks().register("ezyInitFolders", EzyQueryInitTask.class);

    // Use afterEvaluate so that we run AFTER the build script has finished
    // configuring source sets. This way, even if the user reassigns srcDirs
    // with `=` (which wipes earlier additions), our additions survive.
    project.afterEvaluate(
        p -> {
          SourceSetContainer sourceSets = EzyQueryGradleHelper.getSourceSets(p);

          for (SourceSet sourceSet : sourceSets) {
            if (sourceSet.getName().equals("main")) {
              // Auto-discover ezyquery resource dirs and add generated output
              autoConfigureEzyQueryDirs(sourceSet, p);
              sourceSet.getJava().srcDir(ezyBuild.map(EzyQueryBuildTask::getMainOutputDir));
            } else if (sourceSet.getName().equals("test")) {
              autoConfigureEzyQueryDirs(sourceSet, p);
              sourceSet.getJava().srcDir(ezyBuild.map(EzyQueryBuildTask::getTestOutputDir));
            }
          }
        });
  }

  /**
   * Automatically discover ezyquery directories and register them as resource srcDirs. Searches two
   * locations:
   *
   * <ol>
   *   <li>Existing resource srcDirs — any directory named "ezyquery"
   *   <li>Sibling of each java srcDir — e.g. if java srcDir is {@code src/core/main/java}, checks
   *       for {@code src/core/main/ezyquery}
   * </ol>
   *
   * This means the user only needs to place an {@code ezyquery} folder next to their java source
   * folder and the plugin will find it — no manual resource srcDir configuration needed.
   */
  private void autoConfigureEzyQueryDirs(SourceSet sourceSet, Project project) {
    // Strategy 1: check if any resource srcDir is already named "ezyquery"
    Optional<File> existing = EzyQueryGradleHelper.findEzyQuerySourceDirectory(sourceSet);
    if (existing.isPresent()) {
      return; // already configured by the user
    }

    // Strategy 2: look for an "ezyquery" sibling next to each java srcDir
    boolean found = false;
    for (File javaSrcDir : sourceSet.getJava().getSrcDirs()) {
      if (javaSrcDir.getParentFile() == null) continue;
      File ezyQueryDir = new File(javaSrcDir.getParentFile(), "ezyquery");
      if (ezyQueryDir.isDirectory()) {
        logger.lifecycle(
            "EzyQuery: auto-discovered ezyquery dir: {} for sourceSet: {}",
            ezyQueryDir,
            sourceSet.getName());
        sourceSet.getResources().srcDir(ezyQueryDir);
        found = true;
      }
    }
    if (found) return;

    // Strategy 3: fallback to the conventional location (src/main/ezyquery or src/test/ezyquery)
    File conventional = project.file("src/" + sourceSet.getName() + "/ezyquery");
    if (conventional.isDirectory()) {
      sourceSet.getResources().srcDir(conventional);
    }
  }
}
