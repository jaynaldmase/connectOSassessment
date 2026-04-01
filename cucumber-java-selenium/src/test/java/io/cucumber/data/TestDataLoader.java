package io.cucumber.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestDataLoader {
    private static final String TESTDATA_PATH = "testdata/";

    public static Properties load(String fileName) {
        String path = TESTDATA_PATH + fileName;
        try (InputStream is = TestDataLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException(
                    "Test data file not found on classpath: " + path);
            }
            Properties props = new Properties();
            props.load(is);
            return props;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test data: " + path, e);
        }
    }
}
