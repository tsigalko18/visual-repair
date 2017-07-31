package datatype;

import java.io.File;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public abstract class Statement {

	// kind of statement
	WebDriver driverGet;
	WebElement webElement;
	Select select;
	
	// DOM-based information
	private File domBefore;
	private File domAfter;
	private SeleniumLocator domLocator; // on domBefore
	
	// visual-based information
	private File screenshotBefore;
	private File screenshotAfter;
	private File annotatedScreenshot;
	private File visualLocator; // on screenshotBefore
	
	private VisualState visualState;
	
	// DOM-based information
	private File htmlPage;

	private Point coordinates;
	private Dimension dimension;
	
	// statement information
	private String seleniumAction;
	private String value;
	private String name;
	private int line;
	
	public WebDriver getDriverGet() {
		return driverGet;
	}

	public void setDriverGet(WebDriver driverGet) {
		this.driverGet = driverGet;
	}

	public WebElement getWebElement() {
		return webElement;
	}

	public void setWebElement(WebElement webElement) {
		this.webElement = webElement;
	}

	public Select getSelect() {
		return select;
	}

	public void setSelect(Select select) {
		this.select = select;
	}
	
	public File getDomBefore() {
		return domBefore;
	}

	public void setDomBefore(File dom) {
		this.domBefore = dom;
	}
	
	public File getDomAfter() {
		return domAfter;
	}

	public void setDomAfter(File dom) {
		this.domAfter = dom;
	}

	public SeleniumLocator getDomLocator() {
		return domLocator;
	}

	public void setDomLocator(SeleniumLocator domLocator) {
		this.domLocator = domLocator;
	}

	public String getAction() {
		return seleniumAction;
	}

	public void setAction(String action) {
		this.seleniumAction = action;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public File getScreenshotBefore() {
		return screenshotBefore;
	}

	public void setScreenshotBefore(File screenshot) {
		this.screenshotBefore = screenshot;
	}

	public File getScreenshotAfter() {
		return screenshotAfter;
	}

	public void setScreenshotAfter(File screenshot) {
		this.screenshotAfter = screenshot;
	}

	public File getAnnotatedScreenshot() {
		return annotatedScreenshot;
	}

	public void setAnnotatedScreenshot(File annotatedScreenshot) {
		this.annotatedScreenshot = annotatedScreenshot;
	}
	
	public VisualState getVisualState() {
		return visualState;
	}

	public void setVisualState(VisualState visualState) {
		this.visualState = visualState;
	}

	public Point getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(Point coordinates) {
		this.coordinates = coordinates;
	}

	public Dimension getDimension() {
		return dimension;
	}

	public void setDimension(Dimension dimension) {
		this.dimension = dimension;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public String getName() {
		return name;
	}

	public void setName(String statementName) {
		this.name = statementName;
	}

	public File getVisualLocator() {
		return visualLocator;
	}

	public void setVisualLocator(File visualLocator) {
		this.visualLocator = visualLocator;
	}

	public File getHtmlPage() {
		return htmlPage;
	}

	public void setHtmlPage(File htmlPage) {
		this.htmlPage = htmlPage;
	}

	public String getSeleniumAction() {
		return seleniumAction;
	}

	public void setSeleniumAction(String seleniumAction) {
		this.seleniumAction = seleniumAction;
	}

}
