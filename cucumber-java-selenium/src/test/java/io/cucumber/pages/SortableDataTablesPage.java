package io.cucumber.pages;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page object for the Sortable Data Tables page.
 * Handles parsing table#table1 rows into structured data.
 */
public class SortableDataTablesPage extends Page {

  // All body rows in the first table (Example 1 — no class/id on rows)
  @FindBy(css = "table#table1 tbody tr")
  private List<WebElement> tableRows;

  public SortableDataTablesPage(ChromeDriver driver) {
    super(driver);
  }

  /**
   * Parses all rows from table#table1 into a list of maps.
   * Each map has keys: Last Name, First Name, Email, Due, Web Site.
   * @return list of row data maps
   */
  public List<Map<String, String>> getTable1Data() {
    
    // Wait for table rows to be visible before trying to read them
    new WebDriverWait(driver, DEFAULT_TIMEOUT)
        .until(ExpectedConditions.visibilityOfAllElements(tableRows));
    
    // Highlight all table rows so the screenshot shows what we're parsing
    highlightAll(tableRows);

    List<Map<String, String>> rows = new ArrayList<>();
    
    for (WebElement row : tableRows) {
      
      // Each row has 6 cells (5 data + 1 action), we only need the first 5
      List<WebElement> cells = row.findElements(By.tagName("td"));
      
      // LinkedHashMap keeps the columns in insertion order for readability
      Map<String, String> rowData = new LinkedHashMap<>();
      rowData.put("Last Name", cells.get(0).getText());
      rowData.put("First Name", cells.get(1).getText());
      rowData.put("Email", cells.get(2).getText());
      rowData.put("Due", cells.get(3).getText());
      rowData.put("Web Site", cells.get(4).getText());
      rows.add(rowData);
    }
    return rows;
  }
}
