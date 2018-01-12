package com.example.tests;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class AddressBookCheckBirthdayInfoTest {
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
  public void testAddressBookCheckBirthdayInfo() throws Exception {
    driver.get(baseUrl + "/addressbookv8.2.5/addressbook/index.php");
    driver.findElement(By.name("user")).clear();
    driver.findElement(By.name("user")).sendKeys("admin");
    driver.findElement(By.name("pass")).clear();
    driver.findElement(By.name("pass")).sendKeys("secret");
    driver.findElement(By.cssSelector("input[type=\"submit\"]")).click();
    driver.findElement(By.linkText("compleanni")).click();
    assertEquals("19.", driver.findElement(By.xpath(".//*[@id='birthdays']/tbody/tr[2]/td[1]")).getText());
    assertEquals("lastname", driver.findElement(By.xpath(".//*[@id='birthdays']/tbody/tr[2]/td[2]")).getText());
    assertEquals("firstname", driver.findElement(By.xpath(".//*[@id='birthdays']/tbody/tr[2]/td[3]")).getText());
    assertEquals("mail@mail.it", driver.findElement(By.xpath(".//*[@id='birthdays']/tbody/tr[2]/td[5]")).getText());
    assertEquals("01056321", driver.findElement(By.xpath(".//*[@id='birthdays']/tbody/tr[2]/td[6]")).getText());
    assertEquals("Giugno", driver.findElement(By.xpath(".//*[@id='birthdays']/tbody/tr[1]/th")).getText());
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
