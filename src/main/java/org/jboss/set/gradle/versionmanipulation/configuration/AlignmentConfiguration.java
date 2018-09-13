package org.jboss.set.gradle.versionmanipulation.configuration;

public interface AlignmentConfiguration {

    /**
     * Returns aligned version for given project.
     *
     * @return aligned version or null if alignment is not available
     */
    String getProjectVersion();

    /**
     * Obtains aligned version for given dependency.
     *
     * @param group dependency group
     * @param name dependency name
     * @param configuration configuration which dependency belong to
     * @return aligned version or null if alignment is not available
     */
    String getDependencyVersion(String group, String name, String configuration);

}
