package com.example.tests;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class ClarolineRemoveCourseExerciseQuestionsTest {
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
  public void testClarolineRemoveCourseExerciseQuestions() throws Exception {
      driver.get(baseUrl + "/claroline1107/index.php");
      driver.findElement(By.id("login")).clear();
      driver.findElement(By.id("login")).sendKeys("admin");
      driver.findElement(By.id("password")).clear();
      driver.findElement(By.id("password")).sendKeys("admin");
      driver.findElement(By.name("submitAuth")).click();
      driver.findElement(By.linkText("001 - Course001")).click();
      driver.findElement(By.id("CLQWZ")).click();
      driver.findElement(By.xpath("(//img[@alt='Modify'])[2]")).click();
      driver.findElement(By.xpath(".//*[@id='claroBody']/table/tbody/tr[1]/td[6]/a/img")).click();
      assertTrue(closeAlertAndGetItsText().matches("^Are you sure you want to remove the question from the exercise [\\s\\S]$"));
      driver.findElement(By.xpath(".//*[@id='claroBody']/table/tbody/tr[2]/td[6]/a/img")).click();
      assertTrue(closeAlertAndGetItsText().matches("^Are you sure you want to remove the question from the exercise [\\s\\S]$"));
      driver.findElement(By.xpath(".//*[@id='claroBody']/table/tbody/tr[3]/td[6]/a/img")).click();
      assertTrue(closeAlertAndGetItsText().matches("^Are you sure you want to remove the question from the exercise [\\s\\S]$"));
      driver.navigate().refresh();
      // Warning: assertTextNotPresent may require manual changes
      assertFalse(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*Question 1[\\s\\S]*$"));
      // Warning: assertTextNotPresent may require manual changes
      assertFalse(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*Question 2[\\s\\S]*$"));
      // Warning: assertTextNotPresent may require manual changes
      assertFalse(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*Question 3[\\s\\S]*$"));
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
