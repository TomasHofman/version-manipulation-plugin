package org.jboss.set.gradle.versionmanipulation.functionaltest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.jboss.set.gradle.versionmanipulation.action.PublishingRepositoryConfigurer;
import org.jboss.set.gradle.versionmanipulation.functionaltest.utils.TestUtils;
import org.jboss.set.gradle.versionmanipulation.task.GenerateAlignmentPomTask;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@SuppressWarnings("ConstantConditions")
public class SingleProjectBuildFunctTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    private File projectDir;

    @Before
    public void setup() throws IOException, URISyntaxException {
        tempDir.create();
        projectDir = tempDir.newFolder("testproject");
        System.out.println(String.format("Preparing project in %s", projectDir.getPath()));

        TestUtils.copyResourceToDirectory("single-project/build.gradle", projectDir);
        TestUtils.copyResourceToDirectory("single-project/libraries.gradle", projectDir);
        TestUtils.copyResourceToDirectory("single-project/init.gradle", projectDir);
        TestUtils.copyResourceToDirectory("single-project/overrideVersions.properties", projectDir);
        TestUtils.copyResourceToDirectory("single-project/HelloWorld.java", new File(projectDir, "src/main/java/"));

        System.setProperty(PublishingRepositoryConfigurer.URL_SYSTEM_PROPERTY, "http://localhost/testurl");
    }
    @After
    public void tearDown() {
        System.clearProperty(PublishingRepositoryConfigurer.URL_SYSTEM_PROPERTY);
    }

    @Test
    public void testBuild() throws IOException, XmlPullParserException, URISyntaxException {
        // copy prepared pom with aligned versions
        TestUtils.copyResourceToDirectory("single-project/pom.xml", projectDir);

        String[] arguments = {"build", "install", "--info", "--stacktrace"/*, "-I", "init.gradle"*/};
        System.out.println(String.format("Building with arguments %s", Arrays.toString(arguments)));
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(arguments)
                .withPluginClasspath()
                .forwardOutput()
                .build();

        // build task should be successful
        result.getTasks().forEach(buildTask -> System.out.println(buildTask.getPath() + ": " + buildTask.getOutcome()));
        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":build").getOutcome());

        // verify implementation version in manifest
        File manifest = new File(projectDir, "build/tmp/jar/MANIFEST.MF");
        Assert.assertTrue(manifest.exists());
        Properties manifestProps = new Properties();
        manifestProps.load(new FileReader(manifest));
        Assert.assertEquals("1.0.1-redhat-1", manifestProps.getProperty("Implementation-Version"));
        Assert.assertEquals("1.0.1.redhat-1", manifestProps.getProperty("Bundle-Version"));

        // check pom.xml generated by "maven-publish" plugin
        checkPackagedPomXml(new File(projectDir, "build/publications/maven/pom-default.xml"));

        // check pom.xml generated by "maven" plugin
        checkPackagedPomXml(new File(projectDir, "build/poms/pom-default.xml"));

        // check jar file exists
        File jarFile = new File(projectDir, "build/libs/testproject-1.0.1-redhat-1.jar");
        Assert.assertTrue(jarFile.exists());

        // check that pom.xml is included in the jar
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(jarFile))) {
            ZipEntry pomEntry = null;
            while (zipInputStream.available() != 0) {
                ZipEntry entry = zipInputStream.getNextEntry();
                if (entry != null && "META-INF/maven/pom.xml".equals(entry.getName())) {
                    pomEntry = entry;
                    break;
                }
            }
            Assert.assertNotNull(pomEntry);
            // check pom.xml packaged in the jar
            checkPackagedPomXml(zipInputStream);
        }
    }

    @Test
    public void tetsGeneratePom() throws IOException, XmlPullParserException {
        File pomFile = new File(projectDir, "pom.xml");
        Assert.assertFalse(pomFile.exists());

        String[] arguments = {GenerateAlignmentPomTask.NAME, "--info", "--stacktrace"/*, "-I", "init.gradle"*/};
        System.out.println(String.format("Building with arguments %s", Arrays.toString(arguments)));
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(arguments)
                .withPluginClasspath()
                .forwardOutput()
                .build();

        // build task should be successful
        result.getTasks().forEach(buildTask -> System.out.println(buildTask.getPath() + ": " + buildTask.getOutcome()));
        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":" + GenerateAlignmentPomTask.NAME).getOutcome());

        Assert.assertTrue(pomFile.exists());
        checkGeneratedPomXml(pomFile);
    }

    /**
     * Verifies that project and dependency versions were overridden.
     *
     * @param pomFile pom.xml file
     */
    private void checkPackagedPomXml(File pomFile) throws IOException, XmlPullParserException {
        checkPackagedPomXml(new FileInputStream(pomFile));
    }

    /**
     * Verifies that project and dependency versions were overridden.
     *
     * @param is pom.xml input stream
     */
    private void checkPackagedPomXml(InputStream is) throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model pom = reader.read(is);

        // check project version
        Assert.assertEquals("1.0.1-redhat-1", pom.getVersion());

        // check dependency version
        Dependency dependency = pom.getDependencies().get(0);
        Assert.assertEquals("jboss-logging", dependency.getArtifactId());
        Assert.assertEquals("3.3.1.Final-redhat-1", dependency.getVersion());
    }

    /**
     * Checks fresh pom.xml file generated by {@link GenerateAlignmentPomTask}.
     *
     * (Should contain the same version as build.gradle.)
     */
    private void checkGeneratedPomXml(File pomFile) throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model pom = reader.read(new FileInputStream(pomFile));

        // check project version
        Assert.assertEquals("1.0-SNAPSHOT", pom.getVersion());
        Assert.assertEquals("org.jboss.set.gradle", pom.getGroupId());
        Assert.assertEquals("testproject", pom.getArtifactId());
        Assert.assertEquals("pom", pom.getPackaging());

        // check dependency version
        Dependency dependency = pom.getDependencies().get(0);
        Assert.assertEquals("jboss-logging", dependency.getArtifactId());
        Assert.assertEquals("3.3.1.Final", dependency.getVersion());
    }
}
