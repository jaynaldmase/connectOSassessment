package io.cucumber.glue;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.core.Context;
import io.cucumber.core.Manager;
import io.cucumber.pages.HomePage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions for Scenario 1: Verify that the Homepage displays the expected list of example links.
 * Test data is auto-loaded from TEST_TI_0001.properties via getTestData().
 * @see theInternet.feature @TEST_TI_0001
 */
public class HomeSteps extends Context {

  // PicoContainer injects the shared Manager instance
  public HomeSteps(Manager manager) {
    super(manager);
  }

  /**
   * Waits for the homepage to be fully loaded and ready for interaction.
   * Creates a HomePage page object (which triggers PageFactory element init
   * and waitForPageLoad), then stashes it for the next step to use.
   * Matches: When the homepage is fully displayed
   */
  @When("the homepage is fully displayed")
  public void homepageIsFullyDisplayed() {
    
    // Creating the page object triggers the base Page constructor,
    // which waits for document.readyState === 'complete'
    HomePage homePage = new HomePage(getDriver());
    System.out.println("Homepage is fully displayed");
    
    // Stash the page object so the Then step can reuse it
    // without creating a new instance
    stash("homePage", homePage);
  }

  /**
   * Verifies that the homepage displays all the expected example links.
   * Loads expected link names from the properties file and compares
   * them against what's actually on the page.
   * Matches: Then the homepage should display the expected list of example links
   */
  @Then("the homepage should display the expected list of example links")
  public void verifyHomepageLinks() {
    
    // Retrieve the HomePage object we stashed in the When step
    HomePage homePage = (HomePage) getTestStash().get("homePage");
    
    // Get all visible link texts from the homepage content area
    List<String> actualLinks = homePage.getExampleLinkTexts();

    // Auto-load the properties file for this scenario (TEST_TI_0001.properties)
    Properties props = getTestData();
    
    // Build the expected link list from the properties file entries
    List<String> expectedLinks = new ArrayList<>();
    for (int i = 1; i <= Integer.parseInt(props.getProperty("link.count")); i++) {
      expectedLinks.add(props.getProperty("link." + i));
    }

    // Log counts for debugging
    System.out.println("Expected link count: " + expectedLinks.size());
    System.out.println("Actual link count: " + actualLinks.size());

    // Find any expected links that are missing from the page
    List<String> missing = new ArrayList<>(expectedLinks);
    missing.removeAll(actualLinks);
    if (!missing.isEmpty()) {
      System.out.println("Missing links: " + missing);
    }

    // Find any extra links on the page that aren't in our expected list
    List<String> extra = new ArrayList<>(actualLinks);
    extra.removeAll(expectedLinks);
    if (!extra.isEmpty()) {
      System.out.println("Extra links on page: " + extra);
    }

    // Assert that every expected link is present on the page
    // The site may have more links than expected — that's fine,
    // we just need to confirm our expected set is there
    assertTrue(actualLinks.containsAll(expectedLinks),
        "Not all expected links were found on the homepage. Missing: " + missing);
  }
}
