package io.github.kayr.gradle.ezyquery;

import java.io.File;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class EzyQueryCleanTask extends DefaultTask {

  private EzyQueryPluginExtension extension;

  @Inject
  public EzyQueryCleanTask(EzyQueryPluginExtension extension) {
    this.extension = extension;
    setDescription("Clean generated EzyQuery files");
  }

  @TaskAction
  public void clean() {
    File mainOutputDir = extension.mainOutputDir();
    File testOutputDir = extension.testOutputDir();
    if (mainOutputDir.exists()) {
      EzyQueryFileUtils.deleteFolder(mainOutputDir);
    }
    if (testOutputDir.exists()) {
      EzyQueryFileUtils.deleteFolder(testOutputDir);
    }
  }
}
