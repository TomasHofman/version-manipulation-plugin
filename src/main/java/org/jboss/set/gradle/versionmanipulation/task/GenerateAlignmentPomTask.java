package org.jboss.set.gradle.versionmanipulation.task;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelWriter;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.internal.artifacts.dependencies.DefaultSelfResolvingDependency;
import org.gradle.api.tasks.TaskAction;
import org.jboss.set.gradle.versionmanipulation.PluginLogger;

public class GenerateAlignmentPomTask extends DefaultTask {

    public static final String NAME = "generateAlignmentPom";

    @SuppressWarnings("ConstantConditions")
    @TaskAction
    public void perform() throws IOException {
        File pomFile = new File(getProject().getProjectDir(), "pom.xml");

        PluginLogger.ROOT_LOGGER.infof("Generating pom.xml for project %s to %s",
                getProject().getName(), pomFile.getPath());

        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId(valueOrUndefined(getProject().getGroup().toString()));
        model.setArtifactId(getProject().getName());
        model.setVersion(valueOrUndefined(getProject().getVersion().toString()));
        model.setPackaging("pom");

        getProject().getSubprojects().forEach(subproject -> {
            String relativePath = getProject().relativePath(subproject.getProjectDir().getPath());
            PluginLogger.ROOT_LOGGER.infof("Adding module %s for subproject %s", relativePath, subproject.getName());
            model.getModules().add(relativePath);
        });

        getProject().getConfigurations().forEach(configuration ->
            configuration.getDependencies().forEach(dependency -> {
                PluginLogger.ROOT_LOGGER.infof("Adding dependency %s from configuration %s", dependency, configuration);

                if (dependency instanceof DefaultSelfResolvingDependency) {
                    PluginLogger.ROOT_LOGGER.warnf("Don't know how to process dependency of type %s in configuration %s, skipping.",
                            dependency.getClass().getSimpleName(), configuration.getName());
                } else {
                    Validate.notEmpty(dependency.getGroup(), "dependency group is empty in %s", toString(dependency));
                    Validate.notEmpty(dependency.getName(), "dependency name is empty in %s", toString(dependency));
                    Validate.notEmpty(dependency.getVersion(), "dependency version is empty in %s", toString(dependency));

                    Dependency mavenDependency = new Dependency();
                    mavenDependency.setGroupId(dependency.getGroup());
                    mavenDependency.setArtifactId(dependency.getName());
                    mavenDependency.setVersion(dependency.getVersion());
                    model.getDependencies().add(mavenDependency);
                }
            })
        );

        DefaultModelWriter writer = new DefaultModelWriter();
        writer.write(pomFile, null, model);
    }

    private static String valueOrUndefined(String value) {
        if (StringUtils.isEmpty(value)) {
            return "undefined";
        }
        return value;
    }

    private static String toString(org.gradle.api.artifacts.Dependency dependency) {
        return String.format("%s:%s:%s", dependency.getGroup(), dependency.getName(), dependency.getVersion());
    }
}
