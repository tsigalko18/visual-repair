package edu.illinois.reassert.reflect;

import java.util.Deque;
import java.util.LinkedList;

import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.CtScanner;

public class ParentVisitor extends CtScanner {

	private Deque<CtElement> parents = new LinkedList<CtElement>();
	
	public ParentVisitor() {
		parents.push(null);
	}

	public ParentVisitor(CtElement parent) {
		parents.push(parent);
	}

	@Override
	protected void enter(CtElement e) {
		e.setParent(parents.peek());
		parents.push(e);
	}
	
	@Override
	protected void exit(CtElement e) {		
		parents.pop();
	}
	
}
