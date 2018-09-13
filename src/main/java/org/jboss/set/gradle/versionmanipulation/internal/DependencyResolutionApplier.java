package org.jboss.set.gradle.versionmanipulation.internal;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencyResolveDetails;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.jboss.set.gradle.versionmanipulation.PluginLogger;
import org.jboss.set.gradle.versionmanipulation.configuration.AlignmentConfiguration;

public class DependencyResolutionApplier implements Action<Configuration> {

    private Project project;
    private AlignmentConfiguration alignmentConfiguration;

    public DependencyResolutionApplier(Project project, AlignmentConfiguration alignmentConfiguration) {
        this.project = project;
        this.alignmentConfiguration = alignmentConfiguration;
    }

    @Override
    public void execute(Configuration configuration) {
        PluginLogger.ROOT_LOGGER.infof("Applying dependency resolution to configuration %s/%s",
                project.getName(), configuration.getName());

        configuration.getResolutionStrategy().eachDependency(new Action<DependencyResolveDetails>() {
            @Override
            public void execute(DependencyResolveDetails details) {
                ModuleVersionSelector requested = details.getRequested();
                PluginLogger.ROOT_LOGGER.infof("Resolving dependency %s", requested);
                String overriddenVersion = alignmentConfiguration.getDependencyVersion(requested.getGroup(),
                        requested.getName(), configuration.getName());
                String requestedVersion = requested.getVersion();
                if (overriddenVersion != null && !overriddenVersion.equals(requestedVersion)) {
                    PluginLogger.ROOT_LOGGER.infof(" => overriding to %s", overriddenVersion);
                    details.useVersion(overriddenVersion);
                } else {
                    PluginLogger.ROOT_LOGGER.infof(" => no change");
                }
            }
        });
    }
}
