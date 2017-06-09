package edu.illinois.reassert;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtSimpleType;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.AbstractFilter;
import edu.illinois.reassert.reflect.AssertFactory;
import edu.illinois.reassert.reflect.Factory;

/**
 * Default abstract implementation of {@link FixStrategy} 
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public abstract class FixStrategyBase implements FixStrategy {

	private final Factory factory;
	
	public FixStrategyBase(Factory factory) {
		this.factory = factory;
	}
	
	public Factory getFactory() {
		return factory;
	}
	
	/**
	 * @return the {@link AssertFactory} for the given test method
	 */
	protected AssertFactory getAssertFactory(Method testMethod) {
		return getFactory().Assert(testMethod);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<String> getMethodsToInstrument() {
		return null;
	}
	
	/**
	 * @return the file containing the given location
	 */
	protected File findFailingFile(StackTraceElement failureLocation) {
		String packagePath = failureLocation.getClassName();
		int packIndex = packagePath.lastIndexOf('.');
		if (packIndex > -1) {
			packagePath = packagePath.substring(0, packIndex);
			packagePath = packagePath.replace('.', File.separatorChar);
		}
		else {
			packagePath = ""; // root dir
		}
		return new File(packagePath, failureLocation.getFileName());
	}

	/**
	 * @return the {@link CompilationUnit} containing the given location or null
	 */
	protected CompilationUnit findFailingCompilationUnit(StackTraceElement location) {
		File failingFile = findFailingFile(location);
		return getFactory().CompilationUnit().find(failingFile.getPath());
	}

	/**
	 * @return The "innermost" element of type T that contains the given location.
	 * null if no such element exists. 
	 */
	protected <T extends CtElement> T findFailingElement(
			Class<T> elementType,
			final StackTraceElement location) {
		CompilationUnit cu = findFailingCompilationUnit(location);
		if (cu == null) {
			return null;
		}
		for (CtSimpleType<?> type : cu.getDeclaredTypes()) {
			T found = findFailingElement(elementType, type, location);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * @return The "innermost" element of type T that contains the given location
	 * and is a child of the given container.  null if no such element exists.
	 */
	protected <T extends CtElement> T findFailingElement(
			Class<T> elementType,
			CtElement container, 
			final StackTraceElement location) {
		List<T> elements = 
			Query.getElements(container, new AbstractFilter<T>(elementType) {
				@Override
				public boolean matches(T element) {
					return elementContainsLocation(element, location);
				}
			});
		if (elements.size() > 0) {
			// return "innermost" element
			return elements.get(elements.size() - 1);
		}
		return null;
	}
	
	protected boolean elementContainsLocation(CtElement element, StackTraceElement assertLocation) {
		return elementContainsLine(element, assertLocation.getLineNumber());
	}

	protected boolean elementContainsLine(CtElement element, int lineNumber) {
		SourcePosition position = element.getPosition();
		if (position == null) {
			return false;
		}
		return position.getLine() <= lineNumber 
			&& lineNumber <= position.getEndLine();
	}

}
