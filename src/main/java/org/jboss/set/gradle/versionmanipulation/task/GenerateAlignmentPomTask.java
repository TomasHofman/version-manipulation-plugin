package org.jboss.set.gradle.versionmanipulation.task;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.DefaultModelWriter;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.internal.artifacts.dependencies.DefaultSelfResolvingDependency;
import org.gradle.api.tasks.TaskAction;
import org.jboss.set.gradle.versionmanipulation.PluginLogger;

public class GenerateAlignmentPomTask extends DefaultTask {

    public static final String NAME = "generateAlignmentPom";

    @TaskAction
    public void perform() throws IOException {
        Validate.isTrue(getProject().getParent() == null);
        Validate.isTrue(getProject().equals(getProject().getRootProject()));

        Set<Project> discoveredProjects = new HashSet<>();
        LinkedList<Project> projectsToProcess = new LinkedList<>();

        projectsToProcess.add(getProject());
        discoveredProjects.add(getProject());
        while (!projectsToProcess.isEmpty()) {
            Project project = projectsToProcess.poll();
            for (Project subproject: project.getSubprojects()) {
                Validate.isTrue(subproject.getParent() == project);
                if (discoveredProjects.contains(subproject)) {
                    throw PluginLogger.ROOT_LOGGER.processingException("Invalid project tree", null);
                }
                projectsToProcess.add(subproject);
                discoveredProjects.add(subproject);
            }
            writePom(project);
        }
    }

    /**
     * Writes pom.xml file for given project.
     *
     * @param project project which pom to generate
     */
    private static void writePom(Project project) throws IOException {
        File pomFile = new File(project.getProjectDir(), "pom.xml");

        PluginLogger.ROOT_LOGGER.infof("Generating pom.xml for project %s to %s",
                project.getName(), pomFile.getPath());

        Model model = new Model();
        model.setModelVersion("4.0.0");

        Project parent = project.getParent();
        if (parent != null) {
            String pathToParent = project.relativePath(project.getParent().getProjectDir().getPath());

            model.setParent(new Parent());
            model.getParent().setGroupId(valueOrUndefined(parent.getGroup().toString()));
            model.getParent().setArtifactId(parent.getName());
            model.getParent().setVersion(valueOrUndefined(parent.getVersion().toString()));
            model.getParent().setRelativePath(pathToParent);
        }

        model.setGroupId(valueOrUndefined(project.getGroup().toString()));
        model.setArtifactId(project.getName());
        if (parent == null) { // only set for parent project
            model.setVersion(valueOrUndefined(project.getVersion().toString()));
        }
        model.setPackaging("pom");

        for (Project subproject: project.getSubprojects()) {
            String relativePath = project.relativePath(subproject.getProjectDir().getPath());
            PluginLogger.ROOT_LOGGER.infof("Adding module %s for subproject %s", relativePath, subproject.getName());
            model.getModules().add(relativePath);
        }

        project.getConfigurations().forEach(configuration ->
                configuration.getDependencies().forEach(dependency -> {
                    PluginLogger.ROOT_LOGGER.infof("Adding dependency %s from configuration %s", dependency, configuration);

                    if (dependency instanceof DefaultSelfResolvingDependency) {
                        // just log this and ignore
                        String files = ((DefaultSelfResolvingDependency) dependency).getFiles().getFiles().stream().map(File::getPath)
                                .collect(Collectors.joining("\n"));
                        PluginLogger.ROOT_LOGGER.warnf("Skipping dependency of type %s in configuration %s, targetComponentId %s, files:\n%s",
                                dependency.getClass().getSimpleName(), configuration.getName(),
                                ((DefaultSelfResolvingDependency) dependency).getTargetComponentId(),
                                files);
                    } else if (StringUtils.isEmpty(dependency.getGroup()) || StringUtils.isEmpty(dependency.getVersion())) {
                        PluginLogger.ROOT_LOGGER.warnf("Skipping dependency %s, missing group or version.", dependency);
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
