package dev.challenge.finale.util;

import org.junit.runner.Description;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
            .withExposedService("api1", 8080)
            .withExposedService("api2", 8080)
            .withExposedService("api3", 8080)
            .waitingFor("api3", Wait.forLogMessage(".*Assigned partitions: Set\\(data_events.*\n?", 1));

    public static String apiUri(String service) {
        return "http://" +
                container.getServiceHost(service, 8080)
                + ":" +
                container.getServicePort(service, 8080);
    }


}
