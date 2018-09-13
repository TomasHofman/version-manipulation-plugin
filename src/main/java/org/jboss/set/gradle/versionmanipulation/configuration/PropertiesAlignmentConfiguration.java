package org.jboss.set.gradle.versionmanipulation.configuration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.jboss.set.gradle.versionmanipulation.PluginLogger;

/**
 * Loads alignment configuration from a property file.
 */
public class PropertiesAlignmentConfiguration implements AlignmentConfiguration {

    private static final String PROJECT_VERSION_PROPERTY = "project.version";

    private Properties properties = new Properties();

    public PropertiesAlignmentConfiguration(File configFile) {
        try {
            if (!configFile.exists()) {
                PluginLogger.ROOT_LOGGER.warnf("Config location '%s' doesn't exist, no alignments will be performed.",
                        configFile.getPath());
            } else if (!configFile.isFile()) {
                PluginLogger.ROOT_LOGGER.errorf("Config location '%s' is not a file.", configFile.getPath());
            } else {
                properties.load(new FileReader(configFile));
            }
        } catch (IOException e) {
            PluginLogger.ROOT_LOGGER.errorf(e, "Couldn't read property file %s", configFile.getPath());
        }
    }

    @Override
    public String getProjectVersion() {
        return properties.getProperty(PROJECT_VERSION_PROPERTY);
    }

    /**
     * {@inheritDoc}
     *
     * @param configuration currently ignored
     */
    @Override
    public String getDependencyVersion(String group, String name, String configuration) {
        return properties.getProperty(group + ":" + name);
    }
}
