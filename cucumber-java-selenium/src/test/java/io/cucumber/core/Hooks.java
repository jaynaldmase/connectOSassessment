package io.cucumber.core;

import io.cucumber.data.TestDataLoader;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.Scenario;
import java.util.Properties;
import java.util.logging.Level;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

public class Hooks extends Context {

  // Tracks the step number within each scenario for screenshot naming
  private int stepCounter = 0;

  public Hooks(Manager manager) {
    super(manager);
  }

  @Before()
  public void before(Scenario scenario) {
    ChromeOptions options = new ChromeOptions();
    LoggingPreferences logPrefs = new LoggingPreferences();
    logPrefs.enable(LogType.BROWSER, Level.ALL);
    options.addArguments("start-maximized");
    options.setCapability(ChromeOptions.LOGGING_PREFS, logPrefs);
    manager.setDriver(new ChromeDriver(options));
    System.out.println("Made driver");

    // Grab the scenario tag (e.g. @TEST_TI_0001) and stash it
    // This is used by Context.getTestData() to auto-load the right properties file
    String tag = scenario.getSourceTagNames().stream()
        .filter(t -> t.startsWith("@TEST_TI_"))
        .findFirst()
        .orElse("");
    stash("scenarioTag", tag.replace("@", ""));
    System.out.println("Scenario tag: " + tag);

    // Load shared config.properties and stash the base URL
    // This is available to all steps via Context.getBaseUrl()
    // config.properties lives at the root of resources, not in testdata/
    try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
      Properties config = new Properties();
      config.load(is);
      stash("baseUrl", config.getProperty("base.url"));
      System.out.println("Base URL: " + config.getProperty("base.url"));
      // Stash the testdata path so TestDataLoader knows where to find scenario files
      stash("testdataPath", config.getProperty("testdata.path"));
      System.out.println("Test data path: " + config.getProperty("testdata.path"));
    } catch (Exception e) {
      throw new RuntimeException("Failed to load config.properties", e);
    }
  }

  @BeforeStep
  public void beforeStep() {
    stepCounter++;
    System.out.println("Starting step " + stepCounter + "..............................");
  }

  @AfterStep
  public void afterStep(Scenario scenario) {
    byte[] screenshot = getDriver().getScreenshotAs(OutputType.BYTES);
    // Build a descriptive name: e.g. "TEST_TI_0001_Step_02"
    String tag = (String) getTestStash().getOrDefault("scenarioTag", "UNKNOWN");
    String screenshotName = String.format("%s_Step_%02d", tag, stepCounter);
    scenario.attach(screenshot, "image/png", screenshotName);
    System.out.println("Screenshot: " + screenshotName);
    System.out.println("End of step " + stepCounter + "................................");
  }

  @After
  public void after() {
    getDriver().quit();
    System.out.println("Quit driver");
  }

}
