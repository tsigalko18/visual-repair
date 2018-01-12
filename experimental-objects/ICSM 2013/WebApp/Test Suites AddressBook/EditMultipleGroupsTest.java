package com.example.tests;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class AddressBookEditMultipleGroupsTest {
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
  public void testAddressBookEditMultipleGroups() throws Exception {
    driver.get(baseUrl + "/addressbookv8.2.5/addressbook/index.php");
    driver.findElement(By.name("user")).clear();
    driver.findElement(By.name("user")).sendKeys("admin");
    driver.findElement(By.name("pass")).clear();
    driver.findElement(By.name("pass")).sendKeys("secret");
    driver.findElement(By.cssSelector("input[type=\"submit\"]")).click();
    driver.findElement(By.linkText("gruppi")).click();
    driver.findElement(By.xpath(".//*[@id='content']/form/input[4]")).click();
    driver.findElement(By.name("edit")).click();
    driver.findElement(By.name("group_name")).clear();
    driver.findElement(By.name("group_name")).sendKeys("NewGroup1");
    driver.findElement(By.name("group_header")).clear();
    driver.findElement(By.name("group_header")).sendKeys("New Header1");
    driver.findElement(By.name("group_footer")).clear();
    driver.findElement(By.name("group_footer")).sendKeys("New Footer1");
    driver.findElement(By.name("update")).click();
    driver.findElement(By.linkText("group page")).click();
    driver.findElement(By.xpath(".//*[@id='content']/form/input[4]")).click();
    driver.findElement(By.name("edit")).click();
    driver.findElement(By.name("group_name")).clear();
    driver.findElement(By.name("group_name")).sendKeys("NewGroup2");
    driver.findElement(By.name("group_header")).clear();
    driver.findElement(By.name("group_header")).sendKeys("New Header2");
    driver.findElement(By.name("group_footer")).clear();
    driver.findElement(By.name("group_footer")).sendKeys("New Footer2");
    driver.findElement(By.name("update")).click();
    driver.findElement(By.linkText("group page")).click();
    driver.findElement(By.xpath(".//*[@id='content']/form/input[4]")).click();
    driver.findElement(By.name("edit")).click();
    driver.findElement(By.name("group_name")).clear();
    driver.findElement(By.name("group_name")).sendKeys("NewGroup3");
    driver.findElement(By.name("group_header")).clear();
    driver.findElement(By.name("group_header")).sendKeys("New Header3");
    driver.findElement(By.name("group_footer")).clear();
    driver.findElement(By.name("group_footer")).sendKeys("New Footer3");
    driver.findElement(By.name("update")).click();
    driver.findElement(By.linkText("group page")).click();
    assertTrue(driver.findElement(By.xpath(".//*[@id='content']/form")).getText().matches("^[\\s\\S]*NewGroup1[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='content']/form")).getText().matches("^[\\s\\S]*NewGroup2[\\s\\S]*$"));
    assertTrue(driver.findElement(By.xpath(".//*[@id='content']/form")).getText().matches("^[\\s\\S]*NewGroup3[\\s\\S]*$"));
    driver.findElement(By.linkText("homepage")).click();
    new Select(driver.findElement(By.name("group"))).selectByVisibleText("NewGroup1");
    // Warning: assertTextPresent may require manual changes
    assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*New Header1[\\s\\S]*$"));
    // Warning: assertTextPresent may require manual changes
    assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*New Footer1[\\s\\S]*$"));
    new Select(driver.findElement(By.name("group"))).selectByVisibleText("NewGroup2");
    // Warning: assertTextPresent may require manual changes
    assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*New Header2[\\s\\S]*$"));
    // Warning: assertTextPresent may require manual changes
    assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*New Footer2[\\s\\S]*$"));
    new Select(driver.findElement(By.name("group"))).selectByVisibleText("NewGroup3");
    // Warning: assertTextPresent may require manual changes
    assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*New Header3[\\s\\S]*$"));
    // Warning: assertTextPresent may require manual changes
    assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*New Footer3[\\s\\S]*$"));
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
