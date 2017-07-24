package datatype;

import org.openqa.selenium.support.ui.Select;

public class EnhancedWebElement extends Statement {

	// web element != select
	public Select getSelect() { return null; }
	public void setSelect(Select we) { select = null; }
	
	public EnhancedWebElement() {
		super();
	}
	
	@Override
	public String toString() {
		return "driver.findElement(" + getDomLocator() + ")." + getAction() + "(" + getValue() + ");";
	}

	
}
