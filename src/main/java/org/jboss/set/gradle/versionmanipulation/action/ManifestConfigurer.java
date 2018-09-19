package org.jboss.set.gradle.versionmanipulation.action;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.java.archives.internal.DefaultManifest;
import org.gradle.api.plugins.osgi.OsgiManifest;
import org.gradle.api.tasks.bundling.Jar;
import org.jboss.set.gradle.versionmanipulation.PluginLogger;
import org.jboss.set.gradle.versionmanipulation.configuration.AlignmentConfiguration;

/**
 * Overrides specified OSGI manifest entries.
 *
 * Currently only overrides manifest of type OsgiManifest and only entries "Specification-Version" and
 * "Implementation-Version".
 */
public class ManifestConfigurer implements Action<Project> {

    private AlignmentConfiguration alignmentConfiguration;

    public ManifestConfigurer(AlignmentConfiguration alignmentConfiguration) {
        this.alignmentConfiguration = alignmentConfiguration;
    }

    @Override
    public void execute(Project project) {
        project.getTasks().withType(Jar.class, new Action<Jar>() {
            @Override
            public void execute(Jar jar) {
                if (jar.getManifest() instanceof OsgiManifest) {
                    PluginLogger.ROOT_LOGGER.debugf("Overriding OsgiManifest");
                    OsgiManifest manifest = (OsgiManifest) jar.getManifest();
                    if (manifest.getInstructions().containsKey("Implementation-Version")) {
                        manifest.instructionReplace("Implementation-Version", alignmentConfiguration.getProjectVersion());
                    }
                    if (manifest.getInstructions().containsKey("Specification-Version")) {
                        manifest.instructionReplace("Specification-Version", alignmentConfiguration.getProjectVersion());
                    }
                } else if (jar.getManifest() instanceof DefaultManifest) {
                    PluginLogger.ROOT_LOGGER.debugf("Overriding DefaultManifest");
                    // TODO what are common entries here?
                }
            }
        });
    }
}
