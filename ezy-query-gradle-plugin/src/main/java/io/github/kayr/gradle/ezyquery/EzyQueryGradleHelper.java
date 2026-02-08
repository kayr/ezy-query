package io.github.kayr.gradle.ezyquery;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
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

  public static Optional<File> findEzyQuerySourceDirectory(SourceSet sourceSet) {
    Set<File> srcDirs = sourceSet.getResources().getSrcDirs();
    return srcDirs.stream().filter(f -> f.getName().equals("ezyquery")).findFirst();
  }

  public static List<File> findAllEzyQuerySourceDirectories(SourceSet sourceSet) {
    Set<File> srcDirs = sourceSet.getResources().getSrcDirs();
    return srcDirs.stream()
        .filter(f -> f.getName().equals("ezyquery"))
        .collect(Collectors.toList());
  }
}
