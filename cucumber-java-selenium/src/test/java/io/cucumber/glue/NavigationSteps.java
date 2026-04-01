package io.cucumber.glue;

import io.cucumber.data.TestDataLoader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.core.Context;
import io.cucumber.core.Manager;
import java.util.Properties;

/**
 * Shared navigation step definitions used across all scenarios.
 * @see theInternet.feature
 */
public class NavigationSteps extends Context {

  public NavigationSteps(Manager manager) {
    super(manager);
  }

  @Given("the page under test is {string}")
  public void navToPage(String url) {
    manager.getDriver().get(url);
  }

  @When("the user navigates to the {string} page")
  public void navigateToExamplePage(String pageName) {
    Properties props = TestDataLoader.load(
        pageName.toLowerCase().replace(" ", "-") + ".properties");
    String baseUrl = props.getProperty("base.url");
    String pagePath = props.getProperty("page.path");
    manager.getDriver().get(baseUrl + pagePath);
  }
}
