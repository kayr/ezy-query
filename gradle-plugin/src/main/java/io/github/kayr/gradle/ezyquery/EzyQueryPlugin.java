package io.github.kayr.gradle.ezyquery;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class EzyQueryPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {

        //add project extension
        EzyQueryPluginExtension extension = project.getExtensions()
                                             .create("ezyQuery", EzyQueryPluginExtension.class);

        project
          .task("ezyBuild")
          .doLast(task -> {
            System.out.println("Hello from EzyQueryPlugin: "+extension.getSqlScriptPath().get());
        });

    }
}
