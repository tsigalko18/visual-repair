package com.example.tests;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class ClarolineRemoveCourseEventTest {
  private WebDriver driver;
  private String baseUrl;
  private boolean acceptNextAlert = true;
  private StringBuffer verificationErrors = new StringBuffer();

  @Before
  public void setUp() throws Exception {
    driver = new FirefoxDriver();
    baseUrl = "http://localhost/";
    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
  }

  @Test
  public void testClarolineRemoveCourseEvent() throws Exception {
      driver.get(baseUrl + "/claroline1107/index.php?logout=true");
      driver.findElement(By.id("login")).clear();
      driver.findElement(By.id("login")).sendKeys("admin");
      driver.findElement(By.id("password")).clear();
      driver.findElement(By.id("password")).sendKeys("admin");
      driver.findElement(By.name("submitAuth")).click();
      driver.findElement(By.linkText("001 - Course001")).click();
      driver.findElement(By.id("CLCAL")).click();
      driver.findElement(By.linkText("Clear up event list")).click();
      assertTrue(closeAlertAndGetItsText().matches("^Clear up event list [\\s\\S]$"));
      for (int second = 0;; second++) {
      	if (second >= 60) fail("timeout");
      	try { if (isElementPresent(By.xpath(".//*[@id='claroBody']/div[1]/div"))) break; } catch (Exception e) {}
      	Thread.sleep(1000);
      }

      assertEquals("All events deleted from the agenda", driver.findElement(By.xpath(".//*[@id='claroBody']/div[1]/div")).getText());
      driver.findElement(By.linkText("Logout")).click();
  }

  @After
  public void tearDown() throws Exception {
    driver.quit();
    String verificationErrorString = verificationErrors.toString();
    if (!"".equals(verificationErrorString)) {
      fail(verificationErrorString);
    }
  }

  private boolean isElementPresent(By by) {
    try {
      driver.findElement(by);
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  private boolean isAlertPresent() {
    try {
      driver.switchTo().alert();
      return true;
    } catch (NoAlertPresentException e) {
      return false;
    }
  }

  private String closeAlertAndGetItsText() {
    try {
      Alert alert = driver.switchTo().alert();
      String alertText = alert.getText();
      if (acceptNextAlert) {
        alert.accept();
      } else {
        alert.dismiss();
      }
      return alertText;
    } finally {
      acceptNextAlert = true;
    }
  }
}
