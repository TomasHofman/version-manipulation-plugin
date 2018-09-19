package org.jboss.set.gradle.versionmanipulation.action;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.jboss.set.gradle.versionmanipulation.PluginLogger;
import org.jboss.set.gradle.versionmanipulation.configuration.AlignmentConfiguration;

/**
 * Overrides project version.
 */
public class ProjectVersionConfigurer implements Action<Project> {

    private AlignmentConfiguration alignmentConfiguration;

    public ProjectVersionConfigurer(AlignmentConfiguration alignmentConfiguration) {
        this.alignmentConfiguration = alignmentConfiguration;
    }

    @Override
    public void execute(Project project) {
        String targetVersion = alignmentConfiguration.getProjectVersion();
        if (targetVersion != null && !targetVersion.equals(project.getVersion())) {
            PluginLogger.ROOT_LOGGER.infof("Overriding project version from %s to %s on project %s",
                    project.getVersion(), targetVersion, project.getName());
            project.setVersion(targetVersion);
        } else {
            PluginLogger.ROOT_LOGGER.infof("No change on project version on project %s", project.getName());
        }
    }
}
