package com.example.tests;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class ClarolineAddCourseExerciseQuestionsTest {
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
  public void testClarolineAddCourseExerciseQuestions() throws Exception {
      driver.get(baseUrl + "/claroline1107/index.php?logout=true");
      driver.findElement(By.id("login")).clear();
      driver.findElement(By.id("login")).sendKeys("admin");
      driver.findElement(By.id("password")).clear();
      driver.findElement(By.id("password")).sendKeys("admin");
      driver.findElement(By.name("submitAuth")).click();
      driver.findElement(By.linkText("001 - Course001")).click();
      driver.findElement(By.id("CLQWZ")).click();
      driver.findElement(By.xpath("(//img[@alt='Modify'])[2]")).click();
      driver.findElement(By.linkText("New question")).click();
      driver.findElement(By.id("title")).clear();
      driver.findElement(By.id("title")).sendKeys("Question 1");
      driver.findElement(By.id("MCUA")).click();
      driver.findElement(By.cssSelector("input[type=\"submit\"]")).click();
      driver.findElement(By.id("correct_1")).click();
      driver.findElement(By.name("grade_1")).clear();
      driver.findElement(By.name("grade_1")).sendKeys("3");
      driver.findElement(By.name("grade_2")).clear();
      driver.findElement(By.name("grade_2")).sendKeys("-3");
      driver.findElement(By.name("cmdOk")).click();
      driver.findElement(By.linkText("New question")).click();
      driver.findElement(By.id("title")).clear();
      driver.findElement(By.id("title")).sendKeys("Question 2");
      driver.findElement(By.id("TF")).click();
      driver.findElement(By.cssSelector("input[type=\"submit\"]")).click();
      driver.findElement(By.id("trueCorrect")).click();
      driver.findElement(By.name("trueGrade")).clear();
      driver.findElement(By.name("trueGrade")).sendKeys("3");
      driver.findElement(By.name("falseGrade")).clear();
      driver.findElement(By.name("falseGrade")).sendKeys("-3");
      driver.findElement(By.name("cmdOk")).click();
      driver.findElement(By.linkText("New question")).click();
      driver.findElement(By.id("title")).clear();
      driver.findElement(By.id("title")).sendKeys("Question 3");
      driver.findElement(By.id("MCMA")).click();
      driver.findElement(By.cssSelector("input[type=\"submit\"]")).click();
      driver.findElement(By.name("cmdAddAnsw")).click();
      driver.findElement(By.id("correct_1")).click();
      driver.findElement(By.name("grade_1")).clear();
      driver.findElement(By.name("grade_1")).sendKeys("3");
      driver.findElement(By.name("grade_2")).clear();
      driver.findElement(By.name("grade_2")).sendKeys("0");
      driver.findElement(By.name("grade_3")).clear();
      driver.findElement(By.name("grade_3")).sendKeys("-3");
      driver.findElement(By.name("cmdOk")).click();
      driver.findElement(By.linkText("Exercise 001")).click();
      assertEquals("Question 1", driver.findElement(By.xpath(".//*[@id='claroBody']/table/tbody/tr[1]/td[2]")).getText());
      assertEquals("Multiple choice (Unique answer)", driver.findElement(By.xpath(".//*[@id='claroBody']/table/tbody/tr[1]/td[4]")).getText());
      assertEquals("Question 2", driver.findElement(By.xpath(".//*[@id='claroBody']/table/tbody/tr[2]/td[2]")).getText());
      assertEquals("True/False", driver.findElement(By.xpath(".//*[@id='claroBody']/table/tbody/tr[2]/td[4]")).getText());
      assertEquals("Question 3", driver.findElement(By.xpath(".//*[@id='claroBody']/table/tbody/tr[3]/td[2]")).getText());
      assertEquals("Multiple choice (Multiple answers)", driver.findElement(By.xpath(".//*[@id='claroBody']/table/tbody/tr[3]/td[4]")).getText());
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
