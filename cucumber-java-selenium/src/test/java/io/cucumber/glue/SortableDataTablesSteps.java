package io.cucumber.glue;

import io.cucumber.data.TestDataLoader;
import io.cucumber.java.en.Then;
import io.cucumber.core.Context;
import io.cucumber.core.Manager;
import io.cucumber.pages.SortableDataTablesPage;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step definitions for Scenario 3: Sortable Data Tables - Example 1 displays the expected 4 results.
 * @see theInternet.feature @TEST_TI_0003
 */
public class SortableDataTablesSteps extends Context {

  public SortableDataTablesSteps(Manager manager) {
    super(manager);
  }

  @Then("table one should display the expected data")
  public void verifyTable1Data() {
    SortableDataTablesPage page = new SortableDataTablesPage(getDriver());
    List<Map<String, String>> actualRows = page.getTable1Data();

    Properties props = TestDataLoader.load("sortable-tables.properties");
    int rowCount = Integer.parseInt(props.getProperty("row.count"));

    assertEquals(rowCount, actualRows.size(),
        "Expected " + rowCount + " rows but found " + actualRows.size());

    for (int i = 1; i <= rowCount; i++) {
      Map<String, String> actual = actualRows.get(i - 1);
      assertEquals(props.getProperty("row." + i + ".lastName"), actual.get("Last Name"));
      assertEquals(props.getProperty("row." + i + ".firstName"), actual.get("First Name"));
      assertEquals(props.getProperty("row." + i + ".email"), actual.get("Email"));
      assertEquals(props.getProperty("row." + i + ".due"), actual.get("Due"));
      assertEquals(props.getProperty("row." + i + ".webSite"), actual.get("Web Site"));
    }
  }
}
