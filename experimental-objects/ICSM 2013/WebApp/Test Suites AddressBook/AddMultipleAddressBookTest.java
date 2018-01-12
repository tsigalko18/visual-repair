package com.example.tests;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class AddressBookAddMultipleAddressBookTest {
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
  public void testAddressBookAddMultipleAddressBook() throws Exception {
    driver.get(baseUrl + "/addressbookv8.2.5/addressbook/index.php");
    driver.findElement(By.name("user")).clear();
    driver.findElement(By.name("user")).sendKeys("admin");
    driver.findElement(By.name("pass")).clear();
    driver.findElement(By.name("pass")).sendKeys("secret");
    driver.findElement(By.cssSelector("input[type=\"submit\"]")).click();
    driver.findElement(By.linkText("nuovo")).click();
    driver.findElement(By.cssSelector("input[name=\"quickadd\"]")).click();
    driver.findElement(By.name("firstname")).clear();
    driver.findElement(By.name("firstname")).sendKeys("firstname1");
    driver.findElement(By.name("lastname")).clear();
    driver.findElement(By.name("lastname")).sendKeys("lastname1");
    driver.findElement(By.name("address")).clear();
    driver.findElement(By.name("address")).sendKeys("address1");
    driver.findElement(By.name("home")).clear();
    driver.findElement(By.name("home")).sendKeys("01056321");
    driver.findElement(By.name("email")).clear();
    driver.findElement(By.name("email")).sendKeys("mail1@mail.it");
    new Select(driver.findElement(By.name("bday"))).selectByVisibleText("11");
    new Select(driver.findElement(By.name("bmonth"))).selectByVisibleText("Giugno");
    driver.findElement(By.name("byear")).clear();
    driver.findElement(By.name("byear")).sendKeys("1981");
    driver.findElement(By.name("submit")).click();
    driver.findElement(By.linkText("add next")).click();
    driver.findElement(By.cssSelector("input[name=\"quickadd\"]")).click();
    driver.findElement(By.name("firstname")).clear();
    driver.findElement(By.name("firstname")).sendKeys("firstname2");
    driver.findElement(By.name("lastname")).clear();
    driver.findElement(By.name("lastname")).sendKeys("lastname2");
    driver.findElement(By.name("address")).clear();
    driver.findElement(By.name("address")).sendKeys("address2");
    driver.findElement(By.name("home")).clear();
    driver.findElement(By.name("home")).sendKeys("01056322");
    driver.findElement(By.name("email")).clear();
    driver.findElement(By.name("email")).sendKeys("mail2@mail.it");
    new Select(driver.findElement(By.name("bday"))).selectByVisibleText("12");
    new Select(driver.findElement(By.name("bmonth"))).selectByVisibleText("Giugno");
    driver.findElement(By.name("byear")).clear();
    driver.findElement(By.name("byear")).sendKeys("1982");
    driver.findElement(By.name("submit")).click();
    driver.findElement(By.linkText("add next")).click();
    driver.findElement(By.cssSelector("input[name=\"quickadd\"]")).click();
    driver.findElement(By.name("firstname")).clear();
    driver.findElement(By.name("firstname")).sendKeys("firstname3");
    driver.findElement(By.name("lastname")).clear();
    driver.findElement(By.name("lastname")).sendKeys("lastname3");
    driver.findElement(By.name("address")).clear();
    driver.findElement(By.name("address")).sendKeys("address3");
    driver.findElement(By.name("home")).clear();
    driver.findElement(By.name("home")).sendKeys("01056323");
    driver.findElement(By.name("email")).clear();
    driver.findElement(By.name("email")).sendKeys("mail3@mail.it");
    new Select(driver.findElement(By.name("bday"))).selectByVisibleText("13");
    new Select(driver.findElement(By.name("bmonth"))).selectByVisibleText("Giugno");
    driver.findElement(By.name("byear")).clear();
    driver.findElement(By.name("byear")).sendKeys("1983");
    driver.findElement(By.name("submit")).click();
    driver.findElement(By.linkText("homepage")).click();
    assertEquals("Numero di risultati: 3", driver.findElement(By.xpath(".//*[@id='content']/label/strong")).getText());
    assertEquals("lastname1 firstname1", driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[2]/td[3]")).getText());
    assertEquals("mail1@mail.it", driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[2]/td[5]")).getText());
    assertEquals("01056321", driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[2]/td[6]")).getText());
    assertEquals("lastname2 firstname2", driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[3]/td[3]")).getText());
    assertEquals("mail2@mail.it", driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[3]/td[5]")).getText());
    assertEquals("01056322", driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[3]/td[6]")).getText());
    assertEquals("lastname3 firstname3", driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[4]/td[3]")).getText());
    assertEquals("mail3@mail.it", driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[4]/td[5]")).getText());
    assertEquals("01056323", driver.findElement(By.xpath(".//*[@id='maintable']/tbody/tr[4]/td[6]")).getText());
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
