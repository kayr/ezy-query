package io.github.kayr.gradle.ezyquery;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.util.GradleVersion;

public class EzyQueryGradleHelper {


  static SourceSetContainer getSourceSets(Project project) {
    if (isAtLeastGradleVersion("7.1")) {
      JavaPluginExtension byType = project.getExtensions().getByType(JavaPluginExtension.class);
      return byType.getSourceSets();
    } else {
      return getSourceSetsDeprecated(project);
    }
  }

  @SuppressWarnings("deprecation")
  private static SourceSetContainer getSourceSetsDeprecated(Project project) {
    return project
        .getConvention()
        .getPlugin(org.gradle.api.plugins.JavaPluginConvention.class)
        .getSourceSets();
  }

  public static boolean isAtLeastGradleVersion(String version) {
    return GradleVersion.current().getBaseVersion().compareTo(GradleVersion.version(version)) >= 0;
  }
}
