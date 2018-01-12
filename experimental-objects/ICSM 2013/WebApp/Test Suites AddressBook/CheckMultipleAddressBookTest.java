package com.example.tests;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class AddressBookCheckMultipleAddressBookTest {
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
  public void testAddressBookCheckMultipleAddressBook() throws Exception {
    driver.get(baseUrl + "/addressbookv8.2.5/addressbook/index.php");
    driver.findElement(By.name("user")).clear();
    driver.findElement(By.name("user")).sendKeys("admin");
    driver.findElement(By.name("pass")).clear();
    driver.findElement(By.name("pass")).sendKeys("secret");
    driver.findElement(By.cssSelector("input[type=\"submit\"]")).click();
    driver.findElement(By.linkText("stampa numeri telefonici")).click();
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[1]")).getText().matches("^[\\s\\S]*firstname1[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[1]")).getText().matches("^[\\s\\S]*lastname1[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[1]")).getText().matches("^[\\s\\S]*01056321[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[2]")).getText().matches("^[\\s\\S]*firstname2[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[2]")).getText().matches("^[\\s\\S]*lastname2[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[2]")).getText().matches("^[\\s\\S]*01056322[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[3]")).getText().matches("^[\\s\\S]*firstname3[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[3]")).getText().matches("^[\\s\\S]*lastname3[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[3]")).getText().matches("^[\\s\\S]*01056323[\\s\\S]*$"));
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
