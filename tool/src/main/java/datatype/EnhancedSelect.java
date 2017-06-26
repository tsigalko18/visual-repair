package main.java.datatype;

public class EnhancedSelect extends Statement {

	// TODO: adjust toString
	@Override
	public String toString() {
		return "EnhancedSelect [select=" + select + ", domLocator="
		+ getDomLocator() + ", action=" + getAction() + ", value=" + getValue() + ", coordinates=" + getCoordinates() + ", dimension=" + getDimension() + ", name="
		+ getName() + ", line=" + getLine() + "]";
	}
	
}
