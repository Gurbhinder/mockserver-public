package com.test.mockserver;

import com.test.mockserver.watcher.HotReloadWatcher;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;

@SpringBootApplication
public class MockserverApplication {

    private static final String PROPERTIES_FILE = "application.properties";

    public static void main(String[] args) throws Exception {
        Properties props = loadProperties();

        int port = Integer.parseInt(props.getProperty("mockserver.port", "8000"));
        int nioThreads = Integer.parseInt(props.getProperty("mockserver.nio.threads", "10"));
        int actionThreads = Integer.parseInt(props.getProperty("mockserver.action.handler.threads", "10"));
        String foldersCsv = props.getProperty("mockserver.yaml.folders", "get/,other/");

        System.out.println("-------------------------------------------------------");
        System.out.println("Starting MockServer");
        ConfigurationProperties.logLevel("WARN");
        ConfigurationProperties.nioEventLoopThreadCount(nioThreads);
        ConfigurationProperties.actionHandlerThreadCount(actionThreads);
        System.setProperty("mockserver.initializationClass", ExpectationInitialization.class.getName());

        ClientAndServer mockServer = startClientAndServer(port);
        System.out.println("Server started at port:          " + mockServer.getPort());
        System.out.println("Server started at remote address: localhost");
        System.out.println("Server started at status:         " + mockServer.isRunning());
        System.out.println("-------------------------------------------------------");

        HotReloadWatcher watcher = new HotReloadWatcher(mockServer, foldersCsv);
        watcher.start();

        Runtime.getRuntime().addShutdownHook(new Thread(watcher::stop));
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = MockserverApplication.class.getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            System.err.println("Could not load " + PROPERTIES_FILE + ", using defaults");
        }
        return props;
    }
}
