package com.example.tests;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class AddressBookUnassignFromMultipleGroupsTest {
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
  public void testAddressBookUnassignFromMultipleGroups() throws Exception {
    driver.get(baseUrl + "/addressbookv8.2.5/addressbook/index.php");
    driver.findElement(By.name("user")).clear();
    driver.findElement(By.name("user")).sendKeys("admin");
    driver.findElement(By.name("pass")).clear();
    driver.findElement(By.name("pass")).sendKeys("secret");
    driver.findElement(By.cssSelector("input[type=\"submit\"]")).click();
    new Select(driver.findElement(By.name("group"))).selectByVisibleText("NewGroup1");
    driver.findElement(By.xpath("html/body/div[1]/div[4]/form[2]/table/tbody/tr[2]/td[1]/input")).click();
    driver.findElement(By.name("remove")).click();
    driver.findElement(By.linkText("homepage")).click();
    new Select(driver.findElement(By.name("group"))).selectByVisibleText("NewGroup2");
    driver.findElement(By.xpath("html/body/div[1]/div[4]/form[2]/table/tbody/tr[2]/td[1]/input")).click();
    driver.findElement(By.name("remove")).click();
    driver.findElement(By.linkText("homepage")).click();
    new Select(driver.findElement(By.name("group"))).selectByVisibleText("NewGroup3");
    driver.findElement(By.xpath("html/body/div[1]/div[4]/form[2]/table/tbody/tr[2]/td[1]/input")).click();
    driver.findElement(By.name("remove")).click();
    driver.findElement(By.linkText("homepage")).click();
    new Select(driver.findElement(By.name("group"))).selectByVisibleText("NewGroup1");
    assertEquals("Numero di risultati: 0", driver.findElement(By.xpath(".//*[@id='content']/label/strong")).getText());
    new Select(driver.findElement(By.name("group"))).selectByVisibleText("NewGroup2");
    assertEquals("Numero di risultati: 0", driver.findElement(By.xpath(".//*[@id='content']/label/strong")).getText());
    new Select(driver.findElement(By.name("group"))).selectByVisibleText("NewGroup3");
    assertEquals("Numero di risultati: 0", driver.findElement(By.xpath(".//*[@id='content']/label/strong")).getText());
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

  private String closeAlertAndGetItsText() {
    try {
      Alert alert = driver.switchTo().alert();
      if (acceptNextAlert) {
        alert.accept();
      } else {
        alert.dismiss();
      }
      return alert.getText();
    } finally {
      acceptNextAlert = true;
    }
  }
}
