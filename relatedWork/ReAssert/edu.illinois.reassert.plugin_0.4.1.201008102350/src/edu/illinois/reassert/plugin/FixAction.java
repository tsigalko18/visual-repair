package edu.illinois.reassert.plugin;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.junit.model.TestCaseElement;
import org.eclipse.jdt.internal.junit.model.TestRunSession;
import org.eclipse.jdt.junit.model.ITestElement;
import org.eclipse.jdt.junit.model.ITestElementContainer;
import org.eclipse.jdt.junit.model.ITestElement.ProgressState;
import org.eclipse.jdt.junit.model.ITestElement.Result;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.osgi.framework.Bundle;

/**
 * Abstract base class for UI actions that fix failing tests
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
@SuppressWarnings("restriction") // requires TestRunSession to access project
public abstract class FixAction implements IActionDelegate {

	private List<TestCaseElement> testsToFix = null;
	private TestRunSession curSession;

	/**
	 * @return the UI part that triggered the action
	 */
	protected abstract IWorkbenchPart getPart();
	
	/**
	 * @param selectedElement the selected element with which to find the test container
	 * @return the test container holding failing tests to fix
	 */
	protected abstract ITestElement getTestContainer(ITestElement selectedElement);
	
	@Override
	public void run(IAction iaction) {
		if (curSession == null || testsToFix.size() == 0) {
			return;
		}
		Shell shell = getPart().getSite().getShell();
		
		try {
		
			final IJavaProject project = curSession.getLaunchedProject();
			if (project == null) {
				throw new ReAssertException("Cannot find project to launch");
			}

			IOConsole console = findConsole(ReAssertPlugin.CONSOLE_NAME);
			displayConsole(console);
			console.clearConsole();
			final IOConsoleOutputStream outStream = console.newOutputStream();
			final IOConsoleOutputStream errStream = console.newOutputStream();
			errStream.setColor(new Color(null, 0xff, 0, 0));
			
			ReAssertLauncher launcher = new ReAssertLauncher();
			launcher.setProject(project);
			launcher.setOutputStream(new PrintStream(outStream));
			launcher.setErrorStream(new PrintStream(errStream));
			for (TestCaseElement testToFix : testsToFix) {
				String[] testName = testToFix.getTestName().split("[()]");
				launcher.addTestToFix(testName[1], testName[0]);
			}
			findFixStrategyExtensions(launcher);

			ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(shell);
			try {
				progressDialog.run(true, true, launcher);
			} 
			catch (InvocationTargetException e) {
				throw e.getCause();
			} 
			
			TestFixCompareEditorInput compare = new TestFixCompareEditorInput(project);
			compare.setFixFolder(launcher.getOutputFolder());
			
			CompareUI.openCompareDialog(compare);
			
		}
		catch (ReAssertException e) {
			MessageDialog.openError(shell, "ReAssert Error", e.getMessage());
		}
		catch (InterruptedException e) {
			// do nothing. process was cancelled. 
		}
		catch (Throwable e) {
			MessageDialog.openError(shell, "Error", "Unknown error: " +	e.getMessage());
		}
	}

	/**
	 * Find fix strategies provided by plugins that extend the 
	 * {@link ReAssertPlugin#FIX_STRATEGY_EXTENSION_POINT} extension point. 
	 * <br /><br />
	 * TODO: This method is currently (2009-04-28) very fragile.  It simply
	 * adds the extension to the launcher's classpath.  It assumes
	 * that the extension is either an Eclipse plugin project with a bin directory
	 * or a plugin JAR without any external dependencies. 
	 */
	private void findFixStrategyExtensions(ReAssertLauncher launcher)
			throws IOException {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
			.getExtensionPoint(ReAssertPlugin.FIX_STRATEGY_EXTENSION_POINT);
		for (IExtension extension : extensionPoint.getExtensions()) {
			String extensionName = extension.getNamespaceIdentifier();
			Bundle bundle = Platform.getBundle(extensionName);
			File bundleFile = FileLocator.getBundleFile(bundle);
			if (bundleFile.isDirectory()) {
				File binDir = new File(bundleFile, "bin");
				if (binDir.exists()) {
					// Bundle is an Eclipse project (probably a plugin under development). 
					// Assume bin directory contains fixer class.
					launcher.addClasspathEntry(binDir.getPath());
					// TODO: add any contained JARs to launcher's classpath
				}
			} 
			else if (bundleFile.isFile()) {
				// Bundle is a plugin JAR
				launcher.addClasspathEntry(bundleFile.getPath());
				// TODO: add dependencies to launcher's classpath
			}
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				launcher.addFixStrategy(element.getAttribute("class"));
			}
		}
	}
	
	private void displayConsole(IOConsole console) {
		try {
			IWorkbenchPage page = getPart().getSite().getPage();
			IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
			view.display(console);
		} 
		catch (PartInitException e) {
			throw new RuntimeException(e); // TODO: Handle this
		}
	}

	/**
	 * From http://wiki.eclipse.org/FAQ_How_do_I_write_to_the_console_from_a_plug-in
	 */
	private IOConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName())) {
				return (IOConsole) existing[i];
			}
		}
		//	no console found, so create a new one
		IOConsole myConsole = new IOConsole(name, null);
		conMan.addConsoles(new IConsole[]{myConsole});
		return myConsole;
	}
		
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		IStructuredSelection tree = (IStructuredSelection) selection;
		Object firstElem = tree.getFirstElement();
		if (!(firstElem instanceof ITestElement)) {
			return;
		}
		ITestElement testElem = (ITestElement) firstElem;
		
		curSession = (TestRunSession) testElem.getTestRunSession();
		testsToFix = findTestsToFix(getTestContainer(testElem));
		action.setEnabled(testsToFix.size() > 0);		
		
	}
	
	protected List<TestCaseElement> findTestsToFix(ITestElement testElem) {
		List<TestCaseElement> tests = new LinkedList<TestCaseElement>();
		addChildren(tests, testElem);
		return tests;
	}

	private void addChildren(List<TestCaseElement> tests, ITestElement testElem) {
		if (testElem instanceof TestCaseElement && isFailed(testElem)) {			
			tests.add((TestCaseElement) testElem);
		}
		else if (testElem instanceof ITestElementContainer) {
			ITestElement[] children = 
				((ITestElementContainer) testElem).getChildren();
			for (ITestElement child : children) {
				addChildren(tests, child);
			}								
		}
	}

	private boolean isFailed(ITestElement testElem) {
		Result testResult = testElem.getTestResult(true);
		return 
			testElem != null
			&& testElem.getProgressState() == ProgressState.COMPLETED
			&& (testResult == Result.FAILURE
					|| testResult == Result.ERROR);
	}

}
