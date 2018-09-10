package org.jboss.set.gradle4.versionmanipulation.configuration;

public interface ConfigurationStore {

    boolean overrideProjectVersion();

    String getProjectVersion();

    boolean overrideDependencyVersion(String group, String name);

    String getDependencyVersion(String group, String name);

}
