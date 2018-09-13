package org.jboss.set.gradle.versionmanipulation.configuration;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.jboss.set.gradle.versionmanipulation.PluginLogger;

public class MavenPomAlignmentConfiguration implements AlignmentConfiguration {

    private Model pom;

    public MavenPomAlignmentConfiguration(File mavenPom) {
        try {
            if (mavenPom.exists()) {
                PluginLogger.ROOT_LOGGER.infof("Building alignment configuration from file: %s", mavenPom.getPath());

                DefaultModelBuildingRequest request = new DefaultModelBuildingRequest();
                request.setPomFile(mavenPom);
                request.setProcessPlugins(false);

                DefaultModelBuilder builder = new DefaultModelBuilderFactory().newInstance();
                ModelBuildingResult result = builder.build(request);
                pom = result.getEffectiveModel();
            } else {
                PluginLogger.ROOT_LOGGER.warnf("Alignment configuration file not found, alignment will not be performed: %s", mavenPom.getPath());
            }
        } catch (Exception e) {
            throw PluginLogger.ROOT_LOGGER.processingException("Can't process maven pom: " + mavenPom.getPath(), e);
        }
    }

    @Override
    public String getProjectVersion() {
        return pom.getVersion();
    }

    /**
     * {@inheritDoc}
     *
     * @param configuration currently ignored
     */
    @Override
    public String getDependencyVersion(String group, String name, String configuration) {
        // TODO: Take `configuration` parameter into consideration? Now ignored.
        Dependency[] dependencies = pom.getDependencies().stream().filter(
                item -> group.equals(item.getGroupId())
                        && name.equals(item.getArtifactId())
                        /*&& configuration.equals(item.getScope())*/)
                .toArray(Dependency[]::new);

        if (dependencies.length > 2) {
            String options = Arrays.stream(dependencies).map(Dependency::toString).collect(Collectors.joining(", "));
            PluginLogger.ROOT_LOGGER.warnf("Multiple alignment options for dependency %s:%s:%s: %s",
                    group, name, configuration, options);
        }

        return dependencies.length > 0 ? dependencies[0].getVersion() : null;
    }
}
