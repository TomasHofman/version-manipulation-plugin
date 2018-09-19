package org.jboss.set.gradle.versionmanipulation.action;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.jboss.set.gradle.versionmanipulation.configuration.AlignmentConfiguration;

/**
 * Fixes pom.xml generation in "maven-publish" plugin.
 *
 * Applies PomTransformer, that overrides dependencies versions according to given configuration, to all maven
 * publications.
 */
public class PublicationPomTransformerConfigurer implements Action<Project> {

    private AlignmentConfiguration alignmentConfiguration;

    public PublicationPomTransformerConfigurer(AlignmentConfiguration alignmentConfiguration) {
        this.alignmentConfiguration = alignmentConfiguration;
    }

    @Override
    public void execute(Project project) {
        project.getPlugins().withType(MavenPublishPlugin.class, new Action<MavenPublishPlugin>() {
            @Override
            public void execute(MavenPublishPlugin mavenPublishPlugin) {
                project.getExtensions().configure(PublishingExtension.class, new Action<PublishingExtension>() {
                    @Override
                    public void execute(PublishingExtension publishingExtension) {
                        NamedDomainObjectSet<MavenPublication> mavenPublications =
                                publishingExtension.getPublications().withType(MavenPublication.class);
                        mavenPublications.all(new Action<MavenPublication>() {
                            @Override
                            public void execute(MavenPublication mavenPublication) {
                                if (mavenPublication.getPom() != null) {
                                    mavenPublication.getPom().withXml(new PomTransformer(alignmentConfiguration));
                                }
                            }
                        });
                    }
                });
            }
        });
    }
}
