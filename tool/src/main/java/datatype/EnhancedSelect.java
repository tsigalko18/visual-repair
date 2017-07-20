package main.java.datatype;

public class EnhancedSelect extends Statement {

	@Override
	public String toString() {
		return "new Select(driver.findElement(" + getDomLocator() + "))." + getAction() + "(" + getValue() + ");";
	}

}
