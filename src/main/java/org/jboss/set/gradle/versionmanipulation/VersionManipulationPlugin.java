package org.jboss.set.gradle.versionmanipulation;

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.internal.project.ProjectInternal;
import org.jboss.set.gradle.versionmanipulation.action.DependencyResolutionConfigurer;
import org.jboss.set.gradle.versionmanipulation.action.ManifestConfigurer;
import org.jboss.set.gradle.versionmanipulation.action.ProjectVersionConfigurer;
import org.jboss.set.gradle.versionmanipulation.action.PublicationPomTransformerConfigurer;
import org.jboss.set.gradle.versionmanipulation.action.PublishingRepositoryConfigurer;
import org.jboss.set.gradle.versionmanipulation.action.UploadTaskPomTransformerConfigurer;
import org.jboss.set.gradle.versionmanipulation.configuration.AlignmentConfiguration;
import org.jboss.set.gradle.versionmanipulation.configuration.MavenPomAlignmentConfiguration;
import org.jboss.set.gradle.versionmanipulation.task.GenerateAlignmentPomTask;

@SuppressWarnings("unused")
public class VersionManipulationPlugin implements Plugin<ProjectInternal> {

    private static final String CONFIG_FILE_NAME = "overrideVersions.properties";

    @Override
    public void apply(ProjectInternal internalProject) {
        PluginLogger.ROOT_LOGGER.infof("Applying VersionManipulationPlugin to project %s", internalProject.getName());

        // register pom generating task only to the root project
        if (internalProject.getRootProject().getTasks().findByName(GenerateAlignmentPomTask.NAME) == null) {
            internalProject.getRootProject().getTasks().create(GenerateAlignmentPomTask.NAME, GenerateAlignmentPomTask.class);
        }

        // TODO: might be best to separate "generateAlignmentPom" task into separate plugin, since it needs to see original setting,
        // TODO: for now just don't do any overriding if this task is requested
        if (internalProject.getGradle().getStartParameter().getTaskNames().contains(GenerateAlignmentPomTask.NAME)) {
            PluginLogger.ROOT_LOGGER.infof("Generating pom.xml for project %s", internalProject.getName());
        } else {
            //alignmentConfiguration = new PropertiesAlignmentConfiguration(new File(project.getRootProject().getRootDir(), CONFIG_FILE_NAME));
            AlignmentConfiguration alignmentConfiguration = new MavenPomAlignmentConfiguration(new File(internalProject.getProjectDir(), "pom.xml"));

            internalProject.afterEvaluate(new ProjectVersionConfigurer(alignmentConfiguration));
            internalProject.afterEvaluate(new PublishingRepositoryConfigurer());
            internalProject.afterEvaluate(new DependencyResolutionConfigurer(alignmentConfiguration));
            internalProject.afterEvaluate(new UploadTaskPomTransformerConfigurer(alignmentConfiguration));
            internalProject.afterEvaluate(new PublicationPomTransformerConfigurer(alignmentConfiguration));
            internalProject.afterEvaluate(new ManifestConfigurer(alignmentConfiguration));
        }
    }

}
