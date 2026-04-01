package io.cucumber.pages;

import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HomePage extends Page {

  @FindBy(css = "#content ul li a")
  private List<WebElement> exampleLinks;

  public HomePage(ChromeDriver driver) {
    super(driver);
  }

  public List<String> getExampleLinkTexts() {
    new WebDriverWait(driver, DEFAULT_TIMEOUT)
        .until(ExpectedConditions.visibilityOfAllElements(exampleLinks));
    return exampleLinks.stream()
        .map(WebElement::getText)
        .collect(Collectors.toList());
  }
}
