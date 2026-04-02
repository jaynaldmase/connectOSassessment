package io.cucumber.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page object for the Basic Auth page.
 * Accessed via credential-embedded URL to bypass the browser auth dialog.
 */
public class BasicAuthPage extends Page {

  // The paragraph element that shows the success/congratulations message
  @FindBy(css = "#content .example p")
  private WebElement contentText;

  public BasicAuthPage(ChromeDriver driver) {
    super(driver);
  }

  /**
   * Gets the text content of the main paragraph on the auth page.
   * Waits for the element to be visible first.
   * @return the paragraph text (e.g. "Congratulations! You must have the proper credentials.")
   */
  public String getContentText() {
    
    // Make sure the content is actually visible before reading it
    new WebDriverWait(driver, DEFAULT_TIMEOUT)
        .until(ExpectedConditions.visibilityOf(contentText));
    // Highlight the element so the screenshot shows what we're reading
    highlight(contentText);
    return contentText.getText();
  }
}
