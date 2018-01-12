package com.example.tests;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class ClarolineDoCourseExerciseQuestionsMultipleUsersTest {
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
  public void testClarolineDoCourseExerciseQuestionsMultipleUsers() throws Exception {
      driver.get(baseUrl + "/claroline1107/index.php?logout=true");
      driver.findElement(By.id("login")).clear();
      driver.findElement(By.id("login")).sendKeys("testuser1");
      driver.findElement(By.id("password")).clear();
      driver.findElement(By.id("password")).sendKeys("testuser1");
      driver.findElement(By.name("submitAuth")).click();
      driver.findElement(By.linkText("001 - Course001")).click();
      driver.findElement(By.id("CLQWZ")).click();
      driver.findElement(By.linkText("Exercise 001")).click();
      driver.findElement(By.xpath("html/body/div[1]/div[2]/form/table/tbody/tr[2]/td/table/tbody/tr[2]/td[1]/input")).click();
      driver.findElement(By.xpath("html/body/div[1]/div[2]/form/table/tbody/tr[4]/td/table/tbody/tr[2]/td[1]/input")).click();
      driver.findElement(By.xpath("html/body/div[1]/div[2]/form/table/tbody/tr[6]/td/table/tbody/tr[3]/td[1]/input")).click();
      driver.findElement(By.name("cmdOk")).click();
      driver.findElement(By.linkText("Logout")).click();
      driver.findElement(By.id("login")).clear();
      driver.findElement(By.id("login")).sendKeys("testuser2");
      driver.findElement(By.id("password")).clear();
      driver.findElement(By.id("password")).sendKeys("testuser2");
      driver.findElement(By.name("submitAuth")).click();
      driver.findElement(By.linkText("001 - Course001")).click();
      driver.findElement(By.id("CLQWZ")).click();
      driver.findElement(By.linkText("Exercise 001")).click();
      driver.findElement(By.xpath("html/body/div[1]/div[2]/form/table/tbody/tr[2]/td/table/tbody/tr[1]/td[1]/input")).click();
      driver.findElement(By.xpath("html/body/div[1]/div[2]/form/table/tbody/tr[4]/td/table/tbody/tr[2]/td[1]/input")).click();
      driver.findElement(By.xpath("html/body/div[1]/div[2]/form/table/tbody/tr[6]/td/table/tbody/tr[3]/td[1]/input")).click();
      driver.findElement(By.name("cmdOk")).click();
      driver.findElement(By.linkText("Logout")).click();
      driver.findElement(By.id("login")).clear();
      driver.findElement(By.id("login")).sendKeys("testuser3");
      driver.findElement(By.id("password")).clear();
      driver.findElement(By.id("password")).sendKeys("testuser3");
      driver.findElement(By.name("submitAuth")).click();
      driver.findElement(By.linkText("001 - Course001")).click();
      driver.findElement(By.id("CLQWZ")).click();
      driver.findElement(By.linkText("Exercise 001")).click();
      driver.findElement(By.xpath("html/body/div[1]/div[2]/form/table/tbody/tr[2]/td/table/tbody/tr[1]/td[1]/input")).click();
      driver.findElement(By.xpath("html/body/div[1]/div[2]/form/table/tbody/tr[4]/td/table/tbody/tr[1]/td[1]/input")).click();
      driver.findElement(By.xpath("html/body/div[1]/div[2]/form/table/tbody/tr[6]/td/table/tbody/tr[2]/td[1]/input")).click();
      driver.findElement(By.name("cmdOk")).click();
      driver.findElement(By.linkText("Logout")).click();
      driver.findElement(By.id("login")).clear();
      driver.findElement(By.id("login")).sendKeys("admin");
      driver.findElement(By.id("password")).clear();
      driver.findElement(By.id("password")).sendKeys("admin");
      driver.findElement(By.name("submitAuth")).click();
      driver.findElement(By.linkText("001 - Course001")).click();
      driver.findElement(By.id("CLQWZ")).click();
      driver.findElement(By.xpath("(//img[@alt='Statistics'])[2]")).click();
      assertEquals("testuser1 testuser1", driver.findElement(By.xpath(".//*[@id='claroBody']/table[1]/tbody/tr[3]/td[1]/a")).getText());
      assertEquals("-3", driver.findElement(By.xpath(".//*[@id='claroBody']/table[1]/tbody/tr[3]/td[2]")).getText());
      assertEquals("testuser2 testuser2", driver.findElement(By.xpath(".//*[@id='claroBody']/table[1]/tbody/tr[4]/td[1]/a")).getText());
      assertEquals("0", driver.findElement(By.xpath(".//*[@id='claroBody']/table[1]/tbody/tr[4]/td[2]")).getText());
      assertEquals("testuser3 testuser3", driver.findElement(By.xpath(".//*[@id='claroBody']/table[1]/tbody/tr[5]/td[1]/a")).getText());
      assertEquals("6", driver.findElement(By.xpath(".//*[@id='claroBody']/table[1]/tbody/tr[5]/td[2]")).getText());
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
