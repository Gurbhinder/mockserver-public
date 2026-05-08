package com.test.mockserver;

import com.test.mockserver.initializers.YamlInitializer;
import org.mockserver.mock.Expectation;
import org.mockserver.server.initialize.ExpectationInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ExpectationInitialization implements ExpectationInitializer {

    private static final Logger log = LoggerFactory.getLogger(ExpectationInitialization.class);
    private static final String PROPERTIES_FILE = "application.properties";
    private static final String FOLDERS_KEY = "mockserver.yaml.folders";
    private static final String FOLDERS_DEFAULT = "get/,other/";

    @Override
    public Expectation[] initializeExpectations() {
        String foldersCsv = loadProperty(FOLDERS_KEY, FOLDERS_DEFAULT);

        List<String> folders = Arrays.stream(foldersCsv.split(","))
                .map(String::trim)
                .filter(f -> !f.isEmpty())
                .toList();

        List<YamlInitializer> initializers = folders.stream()
                .map(YamlInitializer::new)
                .toList();

        Expectation[] all = initializers.stream()
                .flatMap(i -> Arrays.stream(i.initializeExpectations()))
                .toArray(Expectation[]::new);

        int total = initializers.stream().mapToInt(YamlInitializer::getLoadedCount).sum();
        log.info("Loaded {} folder(s): {}", folders.size(), String.join(", ", folders));
        log.info("Loaded {} mock expectation(s) total", total);

        return all;
    }

    private String loadProperty(String key, String defaultValue) {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            log.error("Could not load {}, using defaults", PROPERTIES_FILE, e);
        }
        return props.getProperty(key, defaultValue);
    }
}
