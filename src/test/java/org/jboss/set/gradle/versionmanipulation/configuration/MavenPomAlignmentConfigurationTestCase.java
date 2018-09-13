package org.jboss.set.gradle.versionmanipulation.configuration;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class MavenPomAlignmentConfigurationTestCase {

    @Test
    public void testSimplePom() throws Exception {
        AlignmentConfiguration config = loadAlignmentConfiguration(getClass().getResource("pom.xml"));

        String version = config.getProjectVersion();
        Assert.assertEquals("1.0.0.redhat-00001", version);

        String dependencyVersion = config.getDependencyVersion("org.jboss.logging", "jboss-logging", null);
        Assert.assertEquals("3.3.2.Final", dependencyVersion);

        dependencyVersion = config.getDependencyVersion("org.jboss.logging", "jboss-logging-annotations", null);
        Assert.assertEquals("2.1.0.Final", dependencyVersion);

        dependencyVersion = config.getDependencyVersion("org.jboss.logging", "jboss-logging-processor", null);
        Assert.assertEquals("2.1.0.Final", dependencyVersion);

        dependencyVersion = config.getDependencyVersion("nonexistent", "nonexistent", null);
        Assert.assertNull(dependencyVersion);
    }

    @Test
    public void testParentPom() throws Exception {
        AlignmentConfiguration config = loadAlignmentConfiguration(getClass().getResource("parentPom.xml"));

        String version = config.getProjectVersion();
        Assert.assertEquals("1.0.0.redhat-00002", version);

        String dependencyVersion = config.getDependencyVersion("org.jboss.logging", "jboss-logging", null);
        Assert.assertNull(dependencyVersion);
    }

    @Test
    public void testChildPom() throws Exception {
        AlignmentConfiguration config = loadAlignmentConfiguration(getClass().getResource("childPom.xml"));

        String version = config.getProjectVersion();
        Assert.assertEquals("1.0.0.redhat-00002", version);


        String dependencyVersion = config.getDependencyVersion("org.jboss.logging", "jboss-logging", null);
        Assert.assertEquals("3.3.2.Final", dependencyVersion);

        dependencyVersion = config.getDependencyVersion("org.jboss.logging", "jboss-logging-annotations", null);
        Assert.assertEquals("2.1.0.Final", dependencyVersion);

        dependencyVersion = config.getDependencyVersion("nonexistent", "nonexistent", null);
        Assert.assertNull(dependencyVersion);
    }

    @Test
    @Ignore
    public void testMavenModelBuilder() throws Exception {
        // just shows how to use the maven-model-builder API

        DefaultModelBuildingRequest request = new DefaultModelBuildingRequest();
        request.setPomFile(new File(getClass().getResource("pom.xml").toURI()));
        request.setProcessPlugins(false);

        DefaultModelBuilder builder = new DefaultModelBuilderFactory().newInstance();
        ModelBuildingResult result = builder.build(request);

        Optional<Dependency> dependency = result.getEffectiveModel().getDependencies().stream()
                .filter(item -> "jboss-logging".equals(item.getArtifactId())).findFirst();
        Assert.assertTrue(dependency.isPresent());
        Assert.assertEquals("3.3.2.Final", dependency.get().getVersion());
        Assert.assertEquals("compile", dependency.get().getScope());

        dependency = result.getEffectiveModel().getDependencies().stream()
                .filter(item -> "jboss-logging-annotations".equals(item.getArtifactId())).findFirst();
        Assert.assertTrue(dependency.isPresent());
        Assert.assertEquals("2.1.0.Final", dependency.get().getVersion());
        Assert.assertEquals("test", dependency.get().getScope());

        dependency = result.getEffectiveModel().getDependencies().stream()
                .filter(item -> "jboss-logging-processor".equals(item.getArtifactId())).findFirst();
        Assert.assertTrue(dependency.isPresent());
        Assert.assertEquals("2.1.0.Final", dependency.get().getVersion());
        Assert.assertEquals("compile", dependency.get().getScope());
    }

    private AlignmentConfiguration loadAlignmentConfiguration(URL mavenPom) throws URISyntaxException {
        return new MavenPomAlignmentConfiguration(new File(mavenPom.toURI()));
    }
}
