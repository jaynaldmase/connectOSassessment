package io.cucumber.glue;

import io.cucumber.data.TestDataLoader;
import io.cucumber.java.en.Then;
import io.cucumber.core.Context;
import io.cucumber.core.Manager;
import io.cucumber.pages.HomePage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions for Scenario 1: Homepage has a list of links to Expected examples.
 * @see theInternet.feature @TEST_TI_0001
 */
public class HomeSteps extends Context {

  public HomeSteps(Manager manager) {
    super(manager);
  }

  @Then("the homepage should display the expected list of example links")
  public void verifyHomepageLinks() {
    HomePage homePage = new HomePage(getDriver());
    List<String> actualLinks = homePage.getExampleLinkTexts();

    Properties props = TestDataLoader.load("homepage-links.properties");
    List<String> expectedLinks = new ArrayList<>();
    for (int i = 1; i <= Integer.parseInt(props.getProperty("link.count")); i++) {
      expectedLinks.add(props.getProperty("link." + i));
    }

    assertEquals(expectedLinks.size(), actualLinks.size(),
        "Link count mismatch: expected " + expectedLinks.size()
            + " but found " + actualLinks.size());
    assertTrue(actualLinks.containsAll(expectedLinks)
            && expectedLinks.containsAll(actualLinks),
        "Homepage links do not match expected list");
  }
}
