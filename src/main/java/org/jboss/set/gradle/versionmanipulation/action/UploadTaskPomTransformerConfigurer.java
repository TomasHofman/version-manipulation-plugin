package org.jboss.set.gradle.versionmanipulation.action;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.maven.MavenResolver;
import org.gradle.api.tasks.Upload;
import org.jboss.set.gradle.versionmanipulation.configuration.AlignmentConfiguration;

/**
 * Fixes pom.xml generation in old "maven" plugin.
 *
 * Adds PomTransformer to all MavenResolver repositories in Upload tasks.
 */
public class UploadTaskPomTransformerConfigurer implements Action<Project> {

    private AlignmentConfiguration alignmentConfiguration;

    public UploadTaskPomTransformerConfigurer(AlignmentConfiguration alignmentConfiguration) {
        this.alignmentConfiguration = alignmentConfiguration;
    }

    @Override
    public void execute(Project project) {
        project.getTasks().withType(Upload.class).all(new Action<Upload>() {
            @Override
            public void execute(Upload upload) {
                upload.getRepositories().withType(MavenResolver.class).all(new Action<MavenResolver>() {
                    @Override
                    public void execute(MavenResolver resolver) {
                        resolver.getPom().withXml(new PomTransformer(alignmentConfiguration));
                    }
                });
            }
        });
    }
}
