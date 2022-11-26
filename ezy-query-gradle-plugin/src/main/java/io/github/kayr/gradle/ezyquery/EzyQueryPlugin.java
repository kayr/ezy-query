package io.github.kayr.gradle.ezyquery;

import io.github.kayr.ezyquery.gen.BatchQueryGen;
import java.io.File;
import java.util.Optional;
import java.util.Set;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.util.GradleVersion;

public class EzyQueryPlugin implements Plugin<Project> {

  private static Logger logger = Logging.getLogger(EzyQueryPlugin.class);

  @Override
  public void apply(Project project) {

    // also apply the java plugin
    project.getPlugins().apply(JavaBasePlugin.class);

    // add project extension
    EzyQueryPluginExtension extension =
        project.getExtensions().create("ezyQuery", EzyQueryPluginExtension.class);

    SourceSetContainer sourceSets = getSourceSets(project);

    sourceSets.configureEach(
        sourceSet -> {
          if (sourceSet.getName().equals("main")) {
            sourceSet.getResources().srcDir("src/main/ezyquery");
            sourceSet.getAllJava().srcDir(extension.mainOutputDir());
          } else if (sourceSet.getName().equals("test")) {
            SourceDirectorySet resources = sourceSet.getResources();
            sourceSet.getAllJava().srcDir(extension.testOutputDir());

            resources.srcDir("src/test/ezyquery");
          }
        });

    project
        .task("ezyBuild")
        .doLast(
            task -> {
              project.getLogger().log(LogLevel.LIFECYCLE, "EzyQuery: Generating Query Classes");

              SourceSetContainer sourceSets1 = getSourceSets(task.getProject());

              for (SourceSet sourceSet : sourceSets1) {

                Set<File> srcDirs = sourceSet.getResources().getSrcDirs();

                Optional<File> ezyQueryDir =
                    srcDirs.stream().filter(f -> f.getName().equals("ezyquery")).findFirst();
                // find the ezyquery folder

                if (!ezyQueryDir.isPresent()) continue;

                if (sourceSet.getName().equals("main")) {
                  File directory = extension.mainOutputDir();

                  if (!directory.exists()) {
                    directory.mkdirs();
                  }

                  logger.log(
                      LogLevel.LIFECYCLE,
                      "Generating main queries to: " + directory + " : From : " + ezyQueryDir);

                  BatchQueryGen batchQueryGen =
                      new BatchQueryGen(ezyQueryDir.get().toPath(), directory.toPath());
                  batchQueryGen.generateAndWrite();

                } else {
                  File directory = extension.testOutputDir();

                  logger.log(
                      LogLevel.LIFECYCLE,
                      "Generating test queries to: " + directory + " : From : " + ezyQueryDir);

                  if (!directory.exists()) {
                    directory.mkdirs();
                  }

                  BatchQueryGen batchQueryGen =
                      new BatchQueryGen(ezyQueryDir.get().toPath(), directory.toPath());
                  batchQueryGen.generateAndWrite();
                }
              }
            });

    project
        .task("ezyClean")
        .doLast(
            task -> {
              File mainOutputDir = extension.mainOutputDir();
              File testOutputDir = extension.testOutputDir();
              if (mainOutputDir.exists()) {
                EzyQueryFileUtils.deleteFolder(mainOutputDir);
              }
              if (testOutputDir.exists()) {
                EzyQueryFileUtils.deleteFolder(testOutputDir);
              }
            });
  }

  private SourceSetContainer getSourceSets(Project project) {
    if (isAtLeastGradleVersion("7.1")) {
      // return
      // project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets();//NOSONAR
      return getSourceSetsDeprecated(project);
    } else {
      return getSourceSetsDeprecated(project);
    }
  }

  @SuppressWarnings("deprecation")
  private SourceSetContainer getSourceSetsDeprecated(Project project) {
    return project
        .getConvention()
        .getPlugin(org.gradle.api.plugins.JavaPluginConvention.class)
        .getSourceSets();
  }

  public static boolean isAtLeastGradleVersion(String version) {
    return GradleVersion.current().getBaseVersion().compareTo(GradleVersion.version(version)) >= 0;
  }
}
