package com.example.tests;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class AddressBookEditMultipleAddressBookTest {
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
  public void testAddressBookEditMultipleAddressBook() throws Exception {
    driver.get(baseUrl + "/addressbookv8.2.5/addressbook/index.php");
    driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[2]/td[8]/a/img")).click();
    driver.findElement(By.name("address")).clear();
    driver.findElement(By.name("address")).sendKeys("newaddress1");
    driver.findElement(By.name("home")).clear();
    driver.findElement(By.name("home")).sendKeys("111111");
    driver.findElement(By.name("email")).clear();
    driver.findElement(By.name("email")).sendKeys("newmail1@mail.it");
    driver.findElement(By.name("update")).click();
    driver.findElement(By.linkText("homepage")).click();
    driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[3]/td[8]/a/img")).click();
    driver.findElement(By.name("address")).clear();
    driver.findElement(By.name("address")).sendKeys("newaddress2");
    driver.findElement(By.name("home")).clear();
    driver.findElement(By.name("home")).sendKeys("222222");
    driver.findElement(By.name("email")).clear();
    driver.findElement(By.name("email")).sendKeys("newmail2@mail.it");
    driver.findElement(By.name("update")).click();
    driver.findElement(By.linkText("homepage")).click();
    driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[4]/td[8]/a/img")).click();
    driver.findElement(By.name("address")).clear();
    driver.findElement(By.name("address")).sendKeys("newaddress3");
    driver.findElement(By.name("home")).clear();
    driver.findElement(By.name("home")).sendKeys("333333");
    driver.findElement(By.name("email")).clear();
    driver.findElement(By.name("email")).sendKeys("newmail3@mail.it");
    driver.findElement(By.name("update")).click();
    driver.findElement(By.linkText("homepage")).click();
    assertEquals("newmail1@mail.it", driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[2]/td[5]")).getText());
    assertEquals("111111", driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[2]/td[6]")).getText());
    assertEquals("newmail2@mail.it", driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[3]/td[5]")).getText());
    assertEquals("222222", driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[3]/td[6]")).getText());
    assertEquals("newmail3@mail.it", driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[4]/td[5]")).getText());
    assertEquals("333333", driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[4]/td[6]")).getText());
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
