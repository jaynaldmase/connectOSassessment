package io.cucumber.pages;

import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import java.time.Duration;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Page {

  protected ChromeDriver driver;
  protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

  public Page(ChromeDriver driver) {
    this.driver = driver;
    PageFactory.initElements(new AppiumFieldDecorator(driver, DEFAULT_TIMEOUT), this);
    waitForPageLoad();
  }

  public void waitForPageLoad() {
    new WebDriverWait(driver, DEFAULT_TIMEOUT)
        .until(ExpectedConditions.jsReturnsValue(
            "return document.readyState === 'complete' ? 'complete' : null"));
  }
}
