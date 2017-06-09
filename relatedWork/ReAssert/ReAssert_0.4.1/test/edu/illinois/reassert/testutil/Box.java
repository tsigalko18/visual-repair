package edu.illinois.reassert.testutil;


/**
 * Simple object used for testing
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class Box {
	private static final int MAX_TOSTRING_NESTING = 3;
	
	private Object value = null;
	
	public Box() {}
	
	public Box(Object value) {
		setValue(value);
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	public Object getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		Object inner = this;
		while (inner instanceof Box && i++ < MAX_TOSTRING_NESTING) {
			sb.append(getClass().getSimpleName()).append("(");
			inner = ((Box)inner).value;
		}
		if (inner instanceof Box) {
			sb.append("...");
			i--;
		}
		else {
			sb.append(inner);
		}
		while (i-- > 0) {
			sb.append(")");
		}
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Box)) {
			return false;
		}
		Box box = (Box)obj;
		return 
			(value == null && box.value == null) 
			|| (value != null && value.equals(box.value));			
	}
	
//	@Override
//	public int hashCode() {
//		return value.hashCode();
//	}
}