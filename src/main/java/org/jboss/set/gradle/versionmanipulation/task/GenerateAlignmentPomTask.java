package org.jboss.set.gradle.versionmanipulation.task;

import java.io.File;
import java.io.IOException;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelWriter;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.jboss.set.gradle.versionmanipulation.PluginLogger;

public class GenerateAlignmentPomTask extends DefaultTask {

    public static final String NAME = "generateAlignmentPom";

    @TaskAction
    public void perform() throws IOException {
        File pomFile = new File(getProject().getProjectDir(), "pom.xml");

        PluginLogger.ROOT_LOGGER.infof("Generating pom.xml for project %s to %s",
                getProject().getName(), pomFile.getPath());

        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId(getProject().getGroup().toString());
        model.setArtifactId(getProject().getName());
        model.setVersion(getProject().getVersion().toString());
        model.setPackaging("pom");

        getProject().getSubprojects().forEach(subproject -> {
            String relativePath = getProject().relativePath(subproject.getProjectDir().getPath());
            model.getModules().add(relativePath);
        });

        getProject().getConfigurations().forEach(configuration ->
            configuration.getDependencies().forEach(dependency -> {
                Dependency mavenDependency = new Dependency();
                mavenDependency.setGroupId(dependency.getGroup());
                mavenDependency.setArtifactId(dependency.getName());
                mavenDependency.setVersion(dependency.getVersion());
                model.getDependencies().add(mavenDependency);
            })
        );

        DefaultModelWriter writer = new DefaultModelWriter();
        writer.write(pomFile, null, model);
    }
}
