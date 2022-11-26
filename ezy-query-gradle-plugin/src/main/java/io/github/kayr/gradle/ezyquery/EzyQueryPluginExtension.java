package io.github.kayr.gradle.ezyquery;

import java.io.File;
import javax.inject.Inject;
import org.gradle.api.file.Directory;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

public class EzyQueryPluginExtension {

  private Property<String> sqlScriptPath;

  private Provider<Directory> outputDir;

  @Inject
  public EzyQueryPluginExtension(ObjectFactory objects, ProjectLayout projectLayout) {

    // get build directory
    outputDir = projectLayout.getBuildDirectory().dir("generated/sources/ezyquery");

    sqlScriptPath = objects.property(String.class).convention("src/main/resources/sql");
  }

  public Provider<Directory> getOutputDir() {
    return outputDir;
  }

  public File mainOutputDir() {
    return fileRelativeToOutput("sources/ezyquery/java/main/");
  }

  public File testOutputDir() {
    return fileRelativeToOutput("sources/ezyquery/java/test/");
  }

  private File fileRelativeToOutput(String child) {
    Directory directory = outputDir.get();
    File parent = directory.getAsFile();
    return new File(parent, child);
  }
}
