package org.jboss.set.gradle.versionmanipulation.action;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.credentials.HttpHeaderCredentials;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.jboss.set.gradle.versionmanipulation.PluginLogger;

/**
 * Adds a "maven-publish" plugin publishing repository specific to PNC environment.
 * <p>
 * System properties "AProxDeployUrl" and "accessToken" has to be defined during build.
 */
public class PublishingRepositoryConfigurer implements Action<Project> {

    public static final String URL_SYSTEM_PROPERTY = "AProxDeployUrl";
    public static final String ACCESS_TOKEN_SYSTEM_PROPERTY = "accessToken";

    @Override
    public void execute(Project project) {
        String pncDeployUrl = System.getProperty(URL_SYSTEM_PROPERTY);
        if (pncDeployUrl != null) {
            project.getPlugins().withType(MavenPublishPlugin.class, plugin -> {
                project.getExtensions().configure(PublishingExtension.class, publishingExtension -> {
                    MavenArtifactRepository repo = publishingExtension.getRepositories().maven(repository -> {
                        repository.setName("PNC");
                        repository.setUrl(pncDeployUrl);
                        repository.credentials(HttpHeaderCredentials.class, passwordCredentials -> {
                            passwordCredentials.setName("Authorization");
                            passwordCredentials.setValue("Bearer " + System.getProperty(ACCESS_TOKEN_SYSTEM_PROPERTY));
                        });
                    });
                    PluginLogger.ROOT_LOGGER.infof("Added publishing repository %s", repo);
                });
            });
        } else {
            PluginLogger.ROOT_LOGGER.debugf("AProxDeployUrl not set, no publishing repository was added.");
        }
    }
}
