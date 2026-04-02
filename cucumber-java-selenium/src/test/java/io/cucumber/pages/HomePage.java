package io.cucumber.pages;

import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page object for the homepage at https://the-internet.herokuapp.com.
 * Handles extracting the list of example links from the content area.
 */
public class HomePage extends Page {

  // Locates all the example links listed on the homepage
  @FindBy(css = "#content ul li a")
  private List<WebElement> exampleLinks;

  public HomePage(ChromeDriver driver) {
    super(driver);
  }

  /**
   * Gets the visible text of every example link on the homepage.
   * Waits for all links to be visible before extracting text.
   * @return list of link text strings
   */
  public List<String> getExampleLinkTexts() {
    
    // Wait until all links are rendered and visible on the page
    new WebDriverWait(driver, DEFAULT_TIMEOUT)
        .until(ExpectedConditions.visibilityOfAllElements(exampleLinks));
    
    // Highlight all links so the screenshot shows what we're checking
    highlightAll(exampleLinks);
    
    // Extract just the text from each link element
    return exampleLinks.stream()
        .map(WebElement::getText)
        .collect(Collectors.toList());
  }
}
