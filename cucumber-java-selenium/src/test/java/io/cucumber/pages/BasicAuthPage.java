package io.cucumber.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BasicAuthPage extends Page {

  @FindBy(css = "#content .example p")
  private WebElement contentText;

  public BasicAuthPage(ChromeDriver driver) {
    super(driver);
  }

  public String getContentText() {
    new WebDriverWait(driver, DEFAULT_TIMEOUT)
        .until(ExpectedConditions.visibilityOf(contentText));
    return contentText.getText();
  }
}
