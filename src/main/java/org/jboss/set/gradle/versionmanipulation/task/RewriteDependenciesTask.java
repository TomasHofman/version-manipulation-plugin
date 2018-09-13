package org.jboss.set.gradle.versionmanipulation.task;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.gradle.api.tasks.TaskAction;
import org.jboss.set.gradle.versionmanipulation.PluginLogger;
import org.jboss.set.gradle.versionmanipulation.configuration.AlignmentConfiguration;

/**
 * This task tries to rewrite dependencies versions directly in gradle configurations.
 *
 * @deprecated This is a dead end, because an attempt to modify DependencySet of given configuration will fail,
 *  if that configuration has already been resolved. Configuration may be resolved e.g. by the build script calling
 *  something like `configurations.compile.each {...}`. The ability to modify dependencies therefore cannot be relied upon.
 */
@Deprecated
public class RewriteDependenciesTask extends DefaultTask {

    public static final String NAME = "rewriteDependencies";

    private AlignmentConfiguration alignmentConfiguration;

    public void setAlignmentConfiguration(AlignmentConfiguration alignmentConfiguration) {
        this.alignmentConfiguration = alignmentConfiguration;
    }

    @SuppressWarnings("unused")
    @TaskAction
    public void perform() {
        if (alignmentConfiguration == null) {
            PluginLogger.ROOT_LOGGER.warnf("Task %s hasn't been configured.", NAME);
            return;
        }
        overrideProjectVersion();
        overrideDependencies();
    }

    private void overrideProjectVersion() {
        String targetVersion = alignmentConfiguration.getProjectVersion();
        if (targetVersion != null) {
            PluginLogger.ROOT_LOGGER.infof("Setting project version for project '%s' to '%s'",
                    getProject().getName(), targetVersion);
            getProject().getProject().setVersion(targetVersion);
        }
    }

    private void overrideDependencies() {
        PluginLogger.ROOT_LOGGER.info("Manipulating dependencies");
        getProject().getConfigurations().forEach(configuration -> {
            PluginLogger.ROOT_LOGGER.infof("Found configuration %s", configuration.getName());

            // collect all dependencies into a list
            List<Dependency> dependencies = new ArrayList<>();
            configuration.getDependencies().forEach(dependency -> {
                PluginLogger.ROOT_LOGGER.infof("Found dependency %s:%s:%s",
                        dependency.getGroup(), dependency.getName(), dependency.getVersion());
                dependencies.add(dependency);
            });

            // and override in separate loop to avoid parallel modification exception
            for (Dependency dependency : dependencies) {
                String newVersion = alignmentConfiguration.getDependencyVersion(
                        dependency.getGroup(), dependency.getName(), configuration.getName());
                if (newVersion != null) {
                    PluginLogger.ROOT_LOGGER.infof("Overriding dependency version from %s:%s:%s to %s",
                            dependency.getGroup(), dependency.getName(), dependency.getVersion(), newVersion);
                    configuration.getDependencies().remove(dependency);
                    configuration.getDependencies().add(
                            new DefaultExternalModuleDependency(dependency.getGroup(), dependency.getName(),
                                    newVersion, configuration.getName()));
                }
            }
        });
    }
}
