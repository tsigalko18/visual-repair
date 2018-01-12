package com.example.tests;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class ClarolineEnrolMultipleUsersTest {
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
  public void testClarolineEnrolMultipleUsers() throws Exception {
      driver.get(baseUrl + "/claroline1107/index.php?logout=true");
      driver.findElement(By.id("login")).clear();
      driver.findElement(By.id("login")).sendKeys("testuser1");
      driver.findElement(By.id("password")).clear();
      driver.findElement(By.id("password")).sendKeys("testuser1");
      driver.findElement(By.name("submitAuth")).click();
      driver.findElement(By.linkText("Enrol on a new course")).click();
      driver.findElement(By.id("keyword")).clear();
      driver.findElement(By.id("keyword")).sendKeys("Course001");
      driver.findElement(By.cssSelector("input[type=\"submit\"]")).click();
      driver.findElement(By.cssSelector("img[alt=\"Enrolment\"]")).click();
      driver.findElement(By.linkText("Logout")).click();
      driver.findElement(By.id("login")).clear();
      driver.findElement(By.id("login")).sendKeys("testuser2");
      driver.findElement(By.id("password")).clear();
      driver.findElement(By.id("password")).sendKeys("testuser2");
      driver.findElement(By.name("submitAuth")).click();
      driver.findElement(By.linkText("Enrol on a new course")).click();
      driver.findElement(By.id("keyword")).clear();
      driver.findElement(By.id("keyword")).sendKeys("Course001");
      driver.findElement(By.cssSelector("input[type=\"submit\"]")).click();
      driver.findElement(By.cssSelector("img[alt=\"Enrolment\"]")).click();
      driver.findElement(By.linkText("Logout")).click();
      driver.findElement(By.id("login")).clear();
      driver.findElement(By.id("login")).sendKeys("testuser3");
      driver.findElement(By.id("password")).clear();
      driver.findElement(By.id("password")).sendKeys("testuser3");
      driver.findElement(By.name("submitAuth")).click();
      driver.findElement(By.linkText("Enrol on a new course")).click();
      driver.findElement(By.id("keyword")).clear();
      driver.findElement(By.id("keyword")).sendKeys("Course001");
      driver.findElement(By.cssSelector("input[type=\"submit\"]")).click();
      driver.findElement(By.cssSelector("img[alt=\"Enrolment\"]")).click();
      driver.findElement(By.linkText("Logout")).click();
      driver.findElement(By.id("login")).clear();
      driver.findElement(By.id("login")).sendKeys("admin");
      driver.findElement(By.id("password")).clear();
      driver.findElement(By.id("password")).sendKeys("admin");
      driver.findElement(By.name("submitAuth")).click();
      driver.findElement(By.linkText("001 - Course001")).click();
      driver.findElement(By.id("CLUSR")).click();
      // Warning: assertTextPresent may require manual changes
      assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*Testuser1[\\s\\S]*$"));
      // Warning: assertTextPresent may require manual changes
      assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*Testuser2[\\s\\S]*$"));
      // Warning: assertTextPresent may require manual changes
      assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*Testuser3[\\s\\S]*$"));
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
