package edu.illinois.reassert.reflect;

import spoon.reflect.cu.CompilationUnit;
import spoon.support.reflect.cu.CompilationUnitImpl;

/**
 * An in-memory {@link CompilationUnit} backed by a {@link VirtualFile}.
 * Used for testing. 
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class VirtualCompilationUnit extends CompilationUnitImpl {
	private VirtualFile source;
	
	public VirtualCompilationUnit(VirtualFile source) {
		this.source = source;
	}
	
	@Override
	public String getOriginalSourceCode() {
		return source.getSource();
	}
}