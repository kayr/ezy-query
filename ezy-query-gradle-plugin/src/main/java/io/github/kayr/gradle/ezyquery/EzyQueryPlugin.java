package io.github.kayr.gradle.ezyquery;

import io.github.kayr.ezyquery.EzyQueryVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaBasePlugin;
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

    // set up :compileJava to depend on

    TaskProvider<EzyQueryBuildTask> ezyBuild =
        project.getTasks().register("ezyBuild", EzyQueryBuildTask.class, extension);

    project.getTasks().register("ezyClean", EzyQueryCleanTask.class, extension);

    project.getTasks().register("ezyInitFolders", EzyQueryInitTask.class);

    SourceSetContainer sourceSets = EzyQueryGradleHelper.getSourceSets(project);

    sourceSets.configureEach(
        sourceSet -> {
          if (sourceSet.getName().equals("main")) {

            sourceSet.getResources().srcDir("src/main/ezyquery");
            sourceSet.getJava().srcDir(ezyBuild.map(EzyQueryBuildTask::getMainOutputDir));

          } else if (sourceSet.getName().equals("test")) {
            sourceSet.getResources().srcDir("src/test/ezyquery");
            sourceSet.getJava().srcDir(ezyBuild.map(EzyQueryBuildTask::getTestOutputDir));
          }
        });
  }
}
