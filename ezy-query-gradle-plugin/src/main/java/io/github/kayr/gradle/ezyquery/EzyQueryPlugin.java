package io.github.kayr.gradle.ezyquery;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.SourceSetContainer;

public class EzyQueryPlugin implements Plugin<Project> {

  private static Logger logger = Logging.getLogger(EzyQueryPlugin.class);

  @Override
  public void apply(Project project) {

    // also apply the java plugin
    project.getPlugins().apply(JavaBasePlugin.class);

    // add project extension
    EzyQueryPluginExtension extension =
        project.getExtensions().create("ezyQuery", EzyQueryPluginExtension.class);

    SourceSetContainer sourceSets = EzyQueryGradleHelper.getSourceSets(project);

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

    project.getTasks().register("ezyBuild", EzyQueryBuildTask.class, extension);

    project.getTasks().register("ezyClean", EzyQueryCleanTask.class, extension);
  }
}
