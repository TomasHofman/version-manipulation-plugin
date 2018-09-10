package org.jboss.set.gradle4.versionmanipulation.internal;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencyResolveDetails;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.jboss.set.gradle4.versionmanipulation.PluginLogger;
import org.jboss.set.gradle4.versionmanipulation.configuration.ConfigurationStore;

public class DependencyResolutionApplier implements Action<Configuration> {

    private Project project;
    private ConfigurationStore configurationStore;

    public DependencyResolutionApplier(Project project, ConfigurationStore configurationStore) {
        this.project = project;
        this.configurationStore = configurationStore;
    }

    @Override
    public void execute(Configuration configuration) {
        PluginLogger.ROOT_LOGGER.infof("Applying dependency resolution to configuration %s on project %s",
                project.getName(), configuration.getName());

        configuration.getResolutionStrategy().eachDependency(new Action<DependencyResolveDetails>() {
            @Override
            public void execute(DependencyResolveDetails details) {
                ModuleVersionSelector requested = details.getRequested();
                PluginLogger.ROOT_LOGGER.infof("Resolving dependency %s", requested);
                String overriddenVersion = configurationStore.getDependencyVersion(requested.getGroup(),
                        requested.getName());
                String requestedVersion = requested.getVersion();
                if (overriddenVersion != null && !overriddenVersion.equals(requestedVersion)) {
//                    PluginLogger.ROOT_LOGGER.errorf("Expected version for dependency '%s:%s' is '%s', but '%s' was requested.",
//                            requested.getGroup(), requested.getName(), overriddenVersion, requested.getVersion());
                    PluginLogger.ROOT_LOGGER.infof("Overriding dependency version from %s:%s:%s to %s",
                            requested.getGroup(), requested.getName(), requested.getVersion(), overriddenVersion);
                    details.useVersion(overriddenVersion);
                }
            }
        });
    }
}
