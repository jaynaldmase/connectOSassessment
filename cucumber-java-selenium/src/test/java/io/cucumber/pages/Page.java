package io.cucumber.pages;

import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.PageFactory;
// Added: WebDriverWait and ExpectedConditions to replace Thread.sleep
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Page {

  protected ChromeDriver driver;

  // Added: shared timeout constant used by all page objects for waits
  protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

  public Page(ChromeDriver driver) {
    this.driver = driver;
    // Changed: use DEFAULT_TIMEOUT instead of hardcoded Duration.ofSeconds(5)
    PageFactory.initElements(new AppiumFieldDecorator(driver, DEFAULT_TIMEOUT), this);
    waitForPageLoad();
  }

  // Changed: replaced Thread.sleep(5s) with WebDriverWait that polls
  // for document.readyState — returns immediately when page is ready
  // instead of always waiting the full 5 seconds
  public void waitForPageLoad() {
    new WebDriverWait(driver, DEFAULT_TIMEOUT)
        .until(ExpectedConditions.jsReturnsValue(
            "return document.readyState === 'complete' ? 'complete' : null"));
  }

  /**
   * Highlights a single element with a red border for visual debugging.
   * Useful for seeing exactly which element the test is interacting with.
   * The highlight stays visible in the @AfterStep screenshot.
   * @param element the WebElement to highlight
   */
  protected void highlight(WebElement element) {
    try {
      ((JavascriptExecutor) driver).executeScript(
          "arguments[0].style.border='3px solid red'", element);
    } catch (Exception e) {
      // Don't fail the test if highlighting doesn't work (e.g. stale element)
      System.out.println("Could not highlight element: " + e.getMessage());
    }
  }

  /**
   * Highlights a list of elements with a red border for visual debugging.
   * @param elements the list of WebElements to highlight
   */
  protected void highlightAll(List<WebElement> elements) {
    for (WebElement element : elements) {
      highlight(element);
    }
  }
}
