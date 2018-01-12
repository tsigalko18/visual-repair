package com.example.tests;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class AddressBookPrintMultipleAddressBookTest {
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
  public void testAddressBookPrintMultipleAddressBook() throws Exception {
    driver.get(baseUrl + "/addressbookv8.2.5/addressbook/index.php");
    driver.findElement(By.linkText("stampa tutto")).click();
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[1]")).getText().matches("^[\\s\\S]*firstname1[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[1]")).getText().matches("^[\\s\\S]*lastname1[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[1]")).getText().matches("^[\\s\\S]*address1[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[1]")).getText().matches("^[\\s\\S]*01056321[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[1]")).getText().matches("^[\\s\\S]*mail1@mail\\.it[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[1]")).getText().matches("^[\\s\\S]*11[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[1]")).getText().matches("^[\\s\\S]*Giugno[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[1]")).getText().matches("^[\\s\\S]*1981[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[2]")).getText().matches("^[\\s\\S]*firstname2[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[2]")).getText().matches("^[\\s\\S]*lastname2[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[2]")).getText().matches("^[\\s\\S]*address2[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[2]")).getText().matches("^[\\s\\S]*01056322[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[2]")).getText().matches("^[\\s\\S]*mail2@mail\\.it[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[2]")).getText().matches("^[\\s\\S]*12[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[2]")).getText().matches("^[\\s\\S]*Giugno[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[2]")).getText().matches("^[\\s\\S]*1982[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[3]")).getText().matches("^[\\s\\S]*firstname3[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[3]")).getText().matches("^[\\s\\S]*lastname3[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[3]")).getText().matches("^[\\s\\S]*address3[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[3]")).getText().matches("^[\\s\\S]*01056323[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[3]")).getText().matches("^[\\s\\S]*mail3@mail\\.it[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[3]")).getText().matches("^[\\s\\S]*13[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[3]")).getText().matches("^[\\s\\S]*Giugno[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='view']/tbody/tr/td[3]")).getText().matches("^[\\s\\S]*1983[\\s\\S]*$"));
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
