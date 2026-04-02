package io.cucumber.core;

import io.cucumber.data.TestDataLoader;
import java.util.HashMap;
import java.util.Properties;
import org.openqa.selenium.chrome.ChromeDriver;

public class Context {

  protected Manager manager;

  public Context(Manager manager) {
    this.manager = manager;
  }

  public ChromeDriver getDriver() {
    return manager.getDriver();
  }

  public HashMap<String, Object> getTestStash() {
    return manager.stash;
  }

  public <T> void stash(String key, T value) {
    System.out.println("Stashing : " + key);
    manager.stash.put(key, value);
  }

  /**
   * Gets the test data properties for the current scenario.
   * Loads once from the properties file matching the scenario tag,
   * then caches in the stash for subsequent calls.
   * @return the Properties object for this scenario's test data
   */
  public Properties getTestData() {
    String key = "scenarioTestData";
    if (!getTestStash().containsKey(key)) {
      // Get the scenario tag that was stashed by Hooks.before()
      String tag = (String) getTestStash().get("scenarioTag");
      // Get the testdata path from config (stashed by Hooks.before())
      String testdataPath = (String) getTestStash().get("testdataPath");
      Properties props = TestDataLoader.load(testdataPath, tag + ".properties");
      stash(key, props);
    }
    return (Properties) getTestStash().get(key);
  }

  /**
   * Gets the base URL from the shared config.properties.
   * Loaded once in Hooks.before() and stashed for all steps to use.
   * @return the base URL string
   */
  public String getBaseUrl() {
    return (String) getTestStash().get("baseUrl");
  }
}
