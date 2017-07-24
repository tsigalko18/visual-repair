package datatype;

import java.io.File;

public class VisualState {

	private File screenshotBefore;
	private File screenshotAfter;
	private File annotatedScreenshot;
	private File visualLocator; // on screenshotBefore
	
	public File getScreenshotBefore() {
		return screenshotBefore;
	}
	public void setScreenshotBefore(File screenshotBefore) {
		this.screenshotBefore = screenshotBefore;
	}
	public File getScreenshotAfter() {
		return screenshotAfter;
	}
	public void setScreenshotAfter(File screenshotAfter) {
		this.screenshotAfter = screenshotAfter;
	}
	public File getAnnotatedScreenshot() {
		return annotatedScreenshot;
	}
	public void setAnnotatedScreenshot(File annotatedScreenshot) {
		this.annotatedScreenshot = annotatedScreenshot;
	}
	public File getVisualLocator() {
		return visualLocator;
	}
	public void setVisualLocator(File visualLocator) {
		this.visualLocator = visualLocator;
	}
	
}
