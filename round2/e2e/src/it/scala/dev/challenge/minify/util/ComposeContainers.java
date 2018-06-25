package dev.challenge.minify.util;

import org.junit.runner.Description;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

//https://github.com/testcontainers/testcontainers-java/issues/674
//https://github.com/testcontainers/testcontainers-java/issues/239
public class ComposeContainers {

    public static Description suiteDescription = Description.createSuiteDescription("Compose tests");

    public static Boolean containersStarted = false;

    private static File composeFile;

    static {
        try {
            composeFile = new File(ComposeContainers.class.getClassLoader().getResource("docker-compose.yml").toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DockerComposeContainer container = new DockerComposeContainer(composeFile)
            .withPull(false)
            .withExposedService("api", 8080)
            .waitingFor("processor", Wait.forLogMessage(".*Listener started.*\n?", 1));

    public static int apiPort() {
        return container.getServicePort("api", 8080);
    }

    public static String apiHost() {
        return container.getServiceHost("api", 8080);
    }

}
