package edu.illinois.reassert.testutil;

public class ExtendedBox extends Box {
	private Object extendedValue;
	
	public ExtendedBox(Object value) {
		super(value);
	}
	
	public ExtendedBox(Object value, Object extendedValue) {
		this(value);
		setExtendedValue(extendedValue);
	}
	
	public void setExtendedValue(Object extendedValue) {
		this.extendedValue = extendedValue;
	}
	public Object getExtendedValue() {
		return extendedValue;
	}
}