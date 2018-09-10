package org.jboss.set.gradle4.versionmanipulation.configuration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.jboss.set.gradle4.versionmanipulation.PluginLogger;

/**
 * Loads alignment configuration from a property file.
 */
public class PropertiesConfigurationStore implements ConfigurationStore {

    private static final String PROJECT_VERSION_PROPERTY = "project.version";

    private Properties properties = new Properties();

    public PropertiesConfigurationStore(File configFile) {
        try {
            if (!configFile.exists()) {
                PluginLogger.ROOT_LOGGER.errorf("Config location '%s' doesn't exist.", configFile.getPath());
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
    public boolean overrideProjectVersion() {
        return getProjectVersion() != null;
    }

    @Override
    public boolean overrideDependencyVersion(String group, String name) {
        return getDependencyVersion(group, name) != null;
    }

    @Override
    public String getProjectVersion() {
        return properties.getProperty(PROJECT_VERSION_PROPERTY);
    }

    @Override
    public String getDependencyVersion(String group, String name) {
        return properties.getProperty(group + ":" + name);
    }
}
