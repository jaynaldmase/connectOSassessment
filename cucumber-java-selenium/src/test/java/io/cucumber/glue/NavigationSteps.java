package io.cucumber.glue;

import io.cucumber.java.en.Given;
import io.cucumber.core.Context;
import io.cucumber.core.Manager;

/**
 * Shared navigation step definitions used across all scenarios.
 * This class handles the common "Given" step that opens the base URL.
 * The base URL comes from config.properties, loaded in Hooks.before().
 * @see theInternet.feature
 */
public class NavigationSteps extends Context {

  // PicoContainer injects the shared Manager instance so we can access the WebDriver
  public NavigationSteps(Manager manager) {
    super(manager);
  }

  /**
   * Opens the base URL from config.properties.
   * This is the entry point for every scenario — it navigates the browser
   * to the homepage using the shared base URL.
   * Matches: Given the page under test is loaded
   */
  @Given("the page under test is loaded")
  public void navToPage() {
    // Get the base URL from shared config (loaded in Hooks.before())
    String url = getBaseUrl();
    System.out.println("Navigating to: " + url);
    // Tell the WebDriver to open the URL in the browser
    manager.getDriver().get(url);
    // Log the page title to confirm we landed on the right page
    System.out.println("Page loaded: " + manager.getDriver().getTitle());
  }
}
