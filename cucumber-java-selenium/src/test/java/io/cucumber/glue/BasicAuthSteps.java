package io.cucumber.glue;

import io.cucumber.data.TestDataLoader;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.core.Context;
import io.cucumber.core.Manager;
import io.cucumber.pages.BasicAuthPage;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions for Scenario 2: Basic Auth allows validated access.
 * @see theInternet.feature @TEST_TI_0002
 */
public class BasicAuthSteps extends Context {

  public BasicAuthSteps(Manager manager) {
    super(manager);
  }

  @When("the user navigates to Basic Auth with valid credentials")
  public void navigateToBasicAuth() {
    Properties props = TestDataLoader.load("basic-auth.properties");
    String baseUrl = props.getProperty("base.url");
    String authPath = props.getProperty("auth.path");
    String url = String.format("https://%s:%s@%s%s",
        props.getProperty("username"), props.getProperty("password"),
        baseUrl.replace("https://", ""), authPath);
    getDriver().get(url);
  }

  @Then("the Basic Auth page should display the congratulations message")
  public void verifyCongratualtionsMessage() {
    Properties props = TestDataLoader.load("basic-auth.properties");
    BasicAuthPage page = new BasicAuthPage(getDriver());
    String expectedText = props.getProperty("expected.text");
    assertTrue(page.getContentText().contains(expectedText),
        "Expected congratulations text not found on page");
  }
}
