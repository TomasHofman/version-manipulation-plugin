package org.jboss.set.gradle.versionmanipulation.functionaltest.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import org.gradle.internal.impldep.com.google.common.io.Files;
import org.junit.Assert;

public final class TestUtils {

    private TestUtils() {}

    public static void copyFile(URL source, File destination) throws URISyntaxException, IOException {
        Assert.assertNotNull(source);
        if (!destination.getParentFile().exists()) {
            Assert.assertTrue(destination.getParentFile().mkdirs());
        }
        Files.copy(new File(source.toURI()), destination);
    }

    public static void copyFileToDirectory(URL source, File targetDirectory) throws IOException, URISyntaxException {
        String fileName = Paths.get(source.getFile()).getFileName().toString();
        copyFile(source, new File(targetDirectory, fileName));
    }

    public static void copyResourceToDirectory(String resource, File targetDirectory) throws IOException, URISyntaxException {
        URL url = TestUtils.class.getClassLoader().getResource(resource);
        copyFileToDirectory(url, targetDirectory);
    }
}
