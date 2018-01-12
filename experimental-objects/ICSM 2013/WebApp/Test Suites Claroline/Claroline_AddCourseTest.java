package com.example.tests;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class ClarolineAddCourseTest {
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
  public void testClarolineAddCourse() throws Exception {
      driver.get(baseUrl + "/claroline1107/index.php");
      driver.findElement(By.id("login")).clear();
      driver.findElement(By.id("login")).sendKeys("admin");
      driver.findElement(By.id("password")).clear();
      driver.findElement(By.id("password")).sendKeys("admin");
      driver.findElement(By.name("submitAuth")).click();
      driver.findElement(By.linkText("Platform administration")).click();
      driver.findElement(By.linkText("Create course")).click();
      driver.findElement(By.id("course_title")).clear();
      driver.findElement(By.id("course_title")).sendKeys("Course001");
      driver.findElement(By.id("course_officialCode")).clear();
      driver.findElement(By.id("course_officialCode")).sendKeys("001");
      // ERROR: Caught exception [ERROR: Unsupported command [addSelection | id=mslist2 | label=Sciences]]
      driver.findElement(By.cssSelector("a.msremove > img")).click();
      // ERROR: Caught exception [ERROR: Unsupported command [addSelection | id=mslist2 | label=Economics]]
      driver.findElement(By.cssSelector("a.msremove > img")).click();
      driver.findElement(By.id("registration_true")).click();
      driver.findElement(By.id("access_public")).click();
      driver.findElement(By.name("changeProperties")).click();
      for (int second = 0;; second++) {
      	if (second >= 60) fail("timeout");
      	try { if (isElementPresent(By.linkText("Continue"))) break; } catch (Exception e) {}
      	Thread.sleep(1000);
      }

      assertEquals("You have just created the course website : 001", driver.findElement(By.xpath(".//*[@id='claroBody']/div[1]/div")).getText());
      driver.findElement(By.linkText("Continue")).click();
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
