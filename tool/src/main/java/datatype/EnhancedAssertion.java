package main.java.datatype;

public class EnhancedAssertion extends EnhancedWebElement {

	// assertTrue(driver.findElement(By.xpath("//*[@class='userName']")).getText().contains("John Doe"));
	String assertion;		// assertTrue
	String predicate; 		// contains("John Doe")
	
	public String getAssertion() { return assertion; }
	public void setAssertion(String assertion) { this.assertion = assertion; }

	public String getPredicate() { return predicate; }
	public void setPredicate(String predicate) { this.predicate = predicate; }

	@Override
	public String toString() {
		return assertion + "(" + getDomLocator() + ")" + "." + predicate + ");";
	}
	
	
}
