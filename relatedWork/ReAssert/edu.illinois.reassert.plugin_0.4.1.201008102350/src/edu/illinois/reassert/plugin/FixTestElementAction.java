package edu.illinois.reassert.plugin;

import org.eclipse.jdt.junit.model.ITestElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action linked to {@link ITestElement}s displayed in the JUnit results window.
 * Triggering the action runs ReAssert to fix the selected test(s). 
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class FixTestElementAction extends FixAction implements IObjectActionDelegate {

	private IWorkbenchPart part;
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.part = targetPart; 
	}

	@Override
	protected IWorkbenchPart getPart() {
		return part;
	}
		
	@Override
	protected ITestElement getTestContainer(ITestElement selectedElement) {
		return selectedElement;
	}
}