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

public class SortableDataTablesPage extends Page {

  @FindBy(css = "table#table1 tbody tr")
  private List<WebElement> tableRows;

  public SortableDataTablesPage(ChromeDriver driver) {
    super(driver);
  }

  public List<Map<String, String>> getTable1Data() {
    new WebDriverWait(driver, DEFAULT_TIMEOUT)
        .until(ExpectedConditions.visibilityOfAllElements(tableRows));
    List<Map<String, String>> rows = new ArrayList<>();
    for (WebElement row : tableRows) {
      List<WebElement> cells = row.findElements(By.tagName("td"));
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
