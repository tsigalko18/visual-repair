package edu.illinois.reassert.reflect;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.declaration.CtSimpleType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

/**
 * Scanner that calculates static method imports for the given model.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class StaticImportScanner extends CtScanner {

	private Set<CtTypeReference<?>> staticImportClasses = new HashSet<CtTypeReference<?>>();
	private Set<CtExecutableReference<?>> staticImports = null;	

	/**
	 * When scanning, records any static methods that are declared in the given class.
	 * Called prior to {@link #makeStaticImports(CompilationUnit)}.
	 */
	public void importStaticFrom(CtTypeReference<?> importFrom) {
		staticImportClasses.add(importFrom);
	}

	/**
	 * @return the static imports for the given compilation unit
	 */
	public Set<CtExecutableReference<?>> makeStaticImports(
			CompilationUnit cu) {
		staticImports = new TreeSet<CtExecutableReference<?>>();
		for (CtSimpleType<?> type : cu.getDeclaredTypes()) {
			scan(type);
		}
		return staticImports;
	}

	@Override
	public <T> void visitCtInvocation(CtInvocation<T> invocation) {
		CtExecutableReference<T> invoked = invocation.getExecutable();
		if (staticImportClasses.contains(invoked.getDeclaringType())
				&& invoked.isStatic()) {
			staticImports.add(invoked);
		}
	}
}
