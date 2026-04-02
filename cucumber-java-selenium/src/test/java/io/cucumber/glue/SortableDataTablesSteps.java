package io.cucumber.glue;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.core.Context;
import io.cucumber.core.Manager;
import io.cucumber.pages.SortableDataTablesPage;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step definitions for Scenario 3: Verify that Sortable Data Tables displays the expected Example 1 results.
 * Test data is auto-loaded from TEST_TI_0003.properties via getTestData().
 * @see theInternet.feature @TEST_TI_0003
 */
public class SortableDataTablesSteps extends Context {

  // PicoContainer injects the shared Manager instance
  public SortableDataTablesSteps(Manager manager) {
    super(manager);
  }

  /**
   * Navigates to the Sortable Data Tables page.
   * Constructs the full URL from base.url + page.path in the properties file.
   * Matches: When the user navigates to Sortable Data Tables
   */
  @When("the user navigates to Sortable Data Tables")
  public void navigateToSortableDataTables() {
    
    // Auto-load the properties file for this scenario (TEST_TI_0003.properties)
    Properties props = getTestData();
    
    // Build the full URL from shared config base URL and page path from test data
    String fullUrl = getBaseUrl() + props.getProperty("page.path");
    System.out.println("Navigating to: " + fullUrl);
    
    // Navigate the browser to the Sortable Data Tables page
    getDriver().get(fullUrl);
    System.out.println("Page loaded: " + getDriver().getTitle());
  }

  /**
   * Verifies that table#table1 (Example 1) contains exactly the expected rows.
   * Parses the table into structured data and compares each row field by field
   * against the expected values from the properties file.
   * Matches: Then the Sortable Data Tables page should display the expected Example 1 data
   */
  @Then("the Sortable Data Tables page should display the expected Example 1 data")
  public void verifyTable1Data() {
    
    // Create the page object — triggers wait for page readyState
    SortableDataTablesPage page = new SortableDataTablesPage(getDriver());
    
    // Parse all rows from table#table1 into a list of maps
    List<Map<String, String>> actualRows = page.getTable1Data();

    // Reuse the same properties (already cached from the When step)
    Properties props = getTestData();
    
    // Get the expected number of rows from properties
    int rowCount = Integer.parseInt(props.getProperty("row.count"));

    // Log counts for debugging
    System.out.println("Expected row count: " + rowCount);
    System.out.println("Actual row count: " + actualRows.size());

    // Log each actual row so we can see what the page returned
    for (int i = 0; i < actualRows.size(); i++) {
      System.out.println("Row " + (i + 1) + ": " + actualRows.get(i));
    }

    // Assert we have the right number of rows
    assertEquals(rowCount, actualRows.size(),
        "Expected " + rowCount + " rows but found " + actualRows.size());

    // Compare each row field by field against expected values from properties
    for (int i = 1; i <= rowCount; i++) {
      Map<String, String> actual = actualRows.get(i - 1);
      // Build the property key prefix for this row (e.g. "row.1.")
      String prefix = "row." + i + ".";
      System.out.println("Verifying row " + i + "...");
      // Check each column value matches the expected value
      assertEquals(props.getProperty(prefix + "lastName"), actual.get("Last Name"));
      assertEquals(props.getProperty(prefix + "firstName"), actual.get("First Name"));
      assertEquals(props.getProperty(prefix + "email"), actual.get("Email"));
      assertEquals(props.getProperty(prefix + "due"), actual.get("Due"));
      assertEquals(props.getProperty(prefix + "webSite"), actual.get("Web Site"));
      System.out.println("Row " + i + " verified OK");
    }
  }
}
