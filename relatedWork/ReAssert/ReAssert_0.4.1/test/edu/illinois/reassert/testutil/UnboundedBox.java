package edu.illinois.reassert.testutil;

public class UnboundedBox extends Box {
	public UnboundedBox(Object value) {
		super(value); 
	}
	@Override
	public Object getValue() {
		return new UnboundedBox(super.getValue());
	}
}