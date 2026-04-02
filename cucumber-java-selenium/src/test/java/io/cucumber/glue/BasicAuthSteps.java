package io.cucumber.glue;

import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.core.Context;
import io.cucumber.core.Manager;
import io.cucumber.pages.BasicAuthPage;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions for Scenario 2: Verify that Basic Auth allows validated access.
 * Test data is auto-loaded from TEST_TI_0002.properties via getTestData().
 * @see theInternet.feature @TEST_TI_0002
 */
public class BasicAuthSteps extends Context {

  // PicoContainer injects the shared Manager instance
  public BasicAuthSteps(Manager manager) {
    super(manager);
  }

  /**
   * Prepares to navigate to the Basic Auth page.
   * This step represents the user's intent to go to Basic Auth.
   * The actual navigation happens in the next step when credentials are supplied.
   * Matches: When the user navigates to Basic Auth
   */
  @When("the user navigates to Basic Auth")
  public void navigateToBasicAuth() {
    System.out.println("Ready to navigate to Basic Auth page");
  }

  /**
   * Supplies valid credentials and navigates to the Basic Auth page.
   * Uses URL-based authentication (https://user:pass@host/path) because
   * Selenium can't reliably interact with browser-native HTTP auth dialogs.
   * Credentials are loaded from the scenario's properties file.
   * Matches: And the user supplies valid credentials
   */
  @And("the user supplies valid credentials")
  public void supplyValidCredentials() {

    // Auto-load the properties file for this scenario
    Properties props = getTestData();

    // Get the base URL from shared config and auth path from test data
    String baseUrl = getBaseUrl();
    String authPath = props.getProperty("auth.path");

    // Build the credential-embedded URL: https://admin:admin@the-internet.herokuapp.com/basic_auth
    // We strip "https://" from baseUrl since we're rebuilding the URL with credentials
    String url = String.format("https://%s:%s@%s%s",
        props.getProperty("username"), props.getProperty("password"),
        baseUrl.replace("https://", ""), authPath);
   
    // Log the URL without credentials for security
    System.out.println("Navigating to Basic Auth with credentials: " + baseUrl + authPath);
    
    // Navigate the browser to the auth page
    getDriver().get(url);
    System.out.println("Basic Auth page loaded successfully");
  }

  /**
   * Verifies that the Basic Auth page displays the congratulations message.
   * Creates a BasicAuthPage page object and checks that the content text
   * contains the expected text from the properties file.
   * Matches: Then the Basic Auth page should display the congratulations message
   */
  @Then("the Basic Auth page should display the congratulations message")
  public void verifyCongratualtionsMessage() {
    
    // Reuse the same properties (already cached from the previous step)
    Properties props = getTestData();
   
    // Create the page object — triggers wait for page readyState
    BasicAuthPage page = new BasicAuthPage(getDriver());
    
    // Get the expected text from properties (e.g. "Congratulations")
    String expectedText = props.getProperty("expected.text");
   
    // Get the actual text displayed on the page
    String actualText = page.getContentText();
    
    // Log both for debugging
    System.out.println("Expected text: " + expectedText);
    System.out.println("Actual page text: " + actualText);
    
    // Use contains() because the full page text is longer than just "Congratulations"
    assertTrue(actualText.contains(expectedText),
        "Expected congratulations text not found on page. Actual: " + actualText);
  }
}
