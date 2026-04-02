package io.cucumber.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for loading test data from .properties files.
 * Keeps test data separate from step definition logic.
 * The testdata path is configurable via config.properties.
 */
public class TestDataLoader {

    /**
     * Loads a properties file from the given base path on the classpath.
     * @param basePath the directory prefix (e.g. "testdata/") from config.properties
     * @param fileName the properties file name (e.g. "TEST_TI_0001.properties")
     * @return the loaded Properties object
     */
    public static Properties load(String basePath, String fileName) {
        // Combine the base path and filename to get the full classpath location
        String path = basePath + fileName;
        // Use classloader to read from src/test/resources at runtime
        try (InputStream is = TestDataLoader.class.getClassLoader().getResourceAsStream(path)) {
            // Fail fast with a clear message if the file doesn't exist
            if (is == null) {
                throw new IllegalArgumentException(
                    "Test data file not found on classpath: " + path);
            }
            Properties props = new Properties();
            props.load(is);
            return props;
        } catch (IOException e) {
            // Wrap checked exception so callers don't need try/catch in test code
            throw new RuntimeException("Failed to load test data: " + path, e);
        }
    }
}
