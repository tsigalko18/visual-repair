package edu.illinois.reassert.plugin;

import org.eclipse.jdt.junit.model.ITestElement;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action linked to the JUnit results window.
 * Triggering the action runs ReAssert to fix all failures in the window.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class FixAllFailuresAction extends FixAction implements IViewActionDelegate {

	private IViewPart part;
	
	@Override
	public void init(IViewPart view) {
		this.part = view;
	}

	@Override
	protected IViewPart getPart() {
		return part;
	}
		
	@Override
	protected ITestElement getTestContainer(ITestElement selectedElement) {
		return selectedElement.getTestRunSession();
	}
}
