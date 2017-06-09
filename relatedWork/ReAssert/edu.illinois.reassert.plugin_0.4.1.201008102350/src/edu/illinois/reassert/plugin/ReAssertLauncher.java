package edu.illinois.reassert.plugin;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.osgi.framework.Bundle;

/**
 * Launches ReAssert in the target project
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class ReAssertLauncher implements IRunnableWithProgress {

	/**
	 * Fully-qualified name of ReAssert's main class
	 */
	private static final String REASSERT_MAIN = "edu.illinois.reassert.ReAssert";
	
	/**
	 * The name of the ReAssert Eclipse project.
	 * Used to allow ReAssert to fix its own tests. 
	 */
	private static final String REASSERT_PROJECT_NAME = "ReAssert";

	/**
	 * The fully-qualified name of the strategy used to fix ReAssert's own tests
	 */
	private static final String REASSERT_STRATEGY = 
		"edu.illinois.reassert.testutil.ReAssertFixer";
	
	/**
	 * JARs required to run external ReAssert process.
	 * Paths are bundle-relative.
	 */
	private static final String[] REASSERT_DEPENDENCIES = new String[] {
		"/lib/reassert.jar",
		"/lib/JSAP-2.1.jar",
		"/lib/junit-4.6.jar",
		"/lib/spoon-core-1.4.jar",
		"/lib/commons-io-1.4.jar",
		"/lib/bcel-5.2.x.jar",
	};

	/**
	 * Where ReAssert should save repaired files. 
	 * Path is target project-relative. 
	 * TODO: this should be a configuration setting
	 */
	public static final String DEFAULT_OUTPUT_FOLDER = ".reassert/output";

	/**
	 * The maximum number of fixes to attempt.
	 * TODO: this should be a configuration setting
	 */
	private static final String DEFAULT_FIX_LIMIT = "10";

	/**
	 * The type of container to use as the source path.
	 * Expects one of the constants in {@link IJavaElement}. 
	 */
	private static final int SOURCE_PATH_LEVEL = IJavaElement.PACKAGE_FRAGMENT_ROOT;
	
	private IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
	private IJavaProject project;
	private PrintStream out = System.out;
	private PrintStream err = System.err;
	private List<String> testsToFix = new LinkedList<String>();
	private List<String> strategies = new LinkedList<String>();
	private List<String> classpathEntries = new LinkedList<String>();

	private volatile boolean launchTerminated = false;

	private IFolder output;
	
	public ReAssertLauncher() {}
	
	public void setProject(IJavaProject project) {
		this.project = project;		
	}

	public void setOutputStream(PrintStream out) {
		this.out = out;
	}

	public void setErrorStream(PrintStream err) {
		this.err = err;
	}
	
	public void addTestToFix(String className, String methodName) {
		this.testsToFix.add(className + '#' + methodName);
	}
	
	public void addFixStrategy(String className) {
		this.strategies.add(className);
	}
	
	public void addClasspathEntry(String entry) {
		this.classpathEntries.add(entry);
	}
	
	/**
	 * @return the VM runner for the given project
	 * @throws ReAssertException if the runner cannot be found
	 */
	protected IVMRunner findRunner(IJavaProject project) {
		IVMInstall vmInstall;
		try {
			vmInstall = JavaRuntime.getVMInstall(project);
		} catch (CoreException e2) {
			vmInstall = null;
		}
		if (vmInstall == null) {
			vmInstall = JavaRuntime.getDefaultVMInstall();
		}
		if (vmInstall == null) {
			throw new ReAssertException(
					"Unable to re-run failed tests. Cannot find VM.");
		}
		final IVMRunner vmRunner = vmInstall.getVMRunner(ILaunchManager.RUN_MODE);
		if (vmRunner == null) {
			throw new ReAssertException(
					"Unable to re-run failed tests. VM does not support launch mode.");
		}
		return vmRunner;
	}
	
	/**
	 * Run ReAssert to fix the tests given in {@link #addTestToFix(String, String)} 
	 * @return the folder containing the fixed files (which may not necessarily contain the target class)
	 * @throws ReAssertException if the launch does not succeed 
	 */
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask("Launching ReAssert", 3);
		
		monitor.subTask("Initialize output folder");
		IPath outPath = 
			project.getPath().append(new Path(DEFAULT_OUTPUT_FOLDER));
		output = initOutputFolder(outPath);
		monitor.worked(1);

		monitor.subTask("Re-run and fix tests");
		final VMRunnerConfiguration vmConfig = 
			configureReAssert(project, output, testsToFix, strategies);
		IVMRunner runner = findRunner(project);
		launchReAssert(runner, vmConfig, new SubProgressMonitor(monitor, 1));
		
		monitor.subTask("Update workspace");
		try {
			output.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 1));
		} 
		catch (CoreException e) {
			throw new ReAssertException("Unable to refresh output folder", e);
		}
		monitor.done();
	}

	public IFolder getOutputFolder() {
		return output;
	}
	
	protected IFolder initOutputFolder(IPath outPath) {
		IFolder output = workspace.getFolder(outPath);
		if (output.exists()) {
			try {
				output.delete(true, new NullProgressMonitor());
			} catch (CoreException e) {
				throw new ReAssertException("Unable to delete output folder", e);
			}
		}
		return output;
	}

	protected VMRunnerConfiguration configureReAssert(
			IJavaProject project, 
			IFolder output,
			List<String> testsToFix, 
			List<String> strategies) {
		
		List<String> cpList = new LinkedList<String>();
		
		cpList.addAll(classpathEntries);
		if (!isReAssert(project)) {
			// Don't include ReAssert JARs when attempting to fix ReAssert itself			
			cpList.addAll(getReAssertClassPath());
		}
		cpList.addAll(getProjectClasspath(project));
		String[] cp = cpList.toArray(new String[] {});

		String[] args = getReAssertArguments(project, output, testsToFix, strategies);

		String workDir = project.getResource().getLocation().toOSString();
		
		final VMRunnerConfiguration vmConfig = 
			new VMRunnerConfiguration(REASSERT_MAIN, cp);
		vmConfig.setProgramArguments(args);
		vmConfig.setWorkingDirectory(workDir);
	
		return vmConfig;
	}

	private boolean isReAssert(IJavaProject project) {
		return REASSERT_PROJECT_NAME.equals(project.getResource().getName());
	}	
	
	protected List<String> getProjectClasspath(IJavaProject project) {
		List<IPath> allPaths = recurseClasspath(project);
		List<String> pathStrings = new LinkedList<String>();
		Set<IPath> knownPaths = new HashSet<IPath>();
		for (IPath path : allPaths) {
			if (!knownPaths.contains(path)) {
				pathStrings.add(path.toOSString());
				knownPaths.add(path);
			}
		}
		return pathStrings;
	}

	protected List<IPath> recurseClasspath(IJavaProject project) {
		List<IPath> cp = new LinkedList<IPath>();
		try {			
			cp.add(makeMachineAbsolute(project.getOutputLocation()));
			IClasspathEntry[] cpEntries = project.getResolvedClasspath(true);
			for (IClasspathEntry entry : cpEntries) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
					IProject otherProject = 
						workspace.getProject(entry.getPath().toOSString());
					IJavaProject otherJavaProject = 
						JavaCore.create(otherProject);	
					cp.addAll(recurseClasspath(otherJavaProject));
				}
				else if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {					
					cp.add(makeMachineAbsolute(entry.getPath()));
				}
				else if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					// individual source folders may specify a custom output location
					IPath entryOutput = entry.getOutputLocation();
					if (entryOutput != null) { 						
						cp.add(makeMachineAbsolute(entryOutput));
					}
				}
				else {
					// getResolvedClasspath should not return 
					// CPE_CONTAINER or CPE_VARIABLE
					throw new RuntimeException();
				}
			}
		} 
		catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
		return cp;
	}

	protected List<String> getReAssertClassPath() {
		Bundle bundle = ReAssertPlugin.getDefault().getBundle();
		List<String> cp = new LinkedList<String>();
		for (String dependency : REASSERT_DEPENDENCIES) {
			try {
				URL depURL = FileLocator.toFileURL(bundle.getEntry(dependency));
				cp.add(depURL.getFile());
			} 
			catch (IOException e) {
				throw new RuntimeException(e); // TODO: Handle this
			}
		}
		return cp;
	}

	protected String[] getReAssertArguments(
			IJavaProject project, 
			IFolder output, 
			List<String> testsToFix, 
			List<String> strategies) {
		List<String> argList = new LinkedList<String>();
		argList.add("--sourcepath");
		argList.add(findSourcePaths(project, testsToFix));
		argList.add("--fixlimit");
		argList.add(DEFAULT_FIX_LIMIT);
		if (isReAssert(project)) {
			strategies.add(REASSERT_STRATEGY);
		}
		if (strategies.size() > 0) {
			argList.add("--fixers");		
			argList.add(join(strategies, File.pathSeparator));
		}
		argList.add("--directory");	
		argList.add(output.getProjectRelativePath().toOSString());
		for (String testToFix : testsToFix) {
			argList.add(testToFix);
		}
		return argList.toArray(new String[] {});
	}

	protected String findSourcePaths(IJavaProject project, List<String> testsToFix) {
		Set<String> sourcePaths = new HashSet<String>();
		for (String testToFix : testsToFix) {
			String className = testToFix
				.substring(0, testToFix.indexOf('#'))
				.replace('$', '.');
			try {
				IType testType = project.findType(className);
				IJavaElement testRoot = testType.getAncestor(SOURCE_PATH_LEVEL);				
				IPath testPath = makeProjectRelative(project, testRoot.getPath());
				if (workspace.getFolder(testRoot.getPath()).isLinked()) {
					// TODO: known bug. Possible fixes: 
					// 1) Pass absolute path to ReAssert. Where to save output?
					// 2) Resolve file paths differently?
					throw new ReAssertException(
							"Unable to repair " + testToFix + 
							" because " + testPath + 
							" is outside of workspace.");
				}
				sourcePaths.add(testPath.toOSString());				
			} catch (JavaModelException e) {
				throw new RuntimeException(e); // TODO: Handle this
			}
		}
		return join(sourcePaths, File.pathSeparator);		
	}
	
	protected IPath makeProjectRelative(IJavaProject project, IPath path) {
		int projectSegments = project.getPath().segmentCount();
		return path.removeFirstSegments(projectSegments);
	}
	
	protected IPath makeMachineAbsolute(IPath path) {
		IFolder folder = workspace.getFolder(path);
		if (folder.exists()) {
			return folder.getLocation();
		}
		IFile file = workspace.getFile(path);
		if (file.exists()) {
			return file.getLocation();
		}
		return path;
	}
	
	private String join(Iterable<String> parts, String glue) {
		StringBuilder sb = new StringBuilder();
		String delim = "";
		for (String part : parts) {
			sb.append(delim);
			sb.append(part);
			delim = glue;
		}
		return sb.toString();
	}

	/**
	 * Executes ReAssert in a separate process and waits for it to complete
	 */
	protected void launchReAssert(
			IVMRunner runner, 
			final VMRunnerConfiguration vmConfig, 
			final IProgressMonitor monitor) throws InterruptedException {
		monitor.beginTask("Re-run and fix tests", 1);
		out.println(configScript(vmConfig));
		
		launchTerminated = false;		
		final ILaunch launch = new Launch(null, ILaunchManager.RUN_MODE, null) {						
			@Override
			protected void fireTerminate() {
				super.fireTerminate();
				launchTerminated = true;
			}
		};							
		
		try {
			// spawns a new thread
			runner.run(vmConfig, launch, null);
		} 
		catch (CoreException e1) {
			throw new ReAssertException("Unable to re-run failed tests. Cannot launch process.");
		}

		IProcess[] processes = launch.getProcesses();
		if (processes.length == 0) {
			throw new ReAssertException("Unable to re-run failed tests. No processes launched.");
		}		
		IStreamsProxy streams = processes[0].getStreamsProxy();
		if (streams != null) {
			streams.getOutputStreamMonitor().addListener(new StreamListener(out));
			streams.getErrorStreamMonitor().addListener(new StreamListener(err));
		}
		
		// wait for process to finish
		while (!launchTerminated) {
			if (monitor.isCanceled()) {
				try {
					launch.terminate();
				} 
				catch (DebugException e) {
					// Don't care. Just kill it.
				}
				err.println("Cancelled!");
				throw new InterruptedException();
			}
		}				
				
		monitor.worked(1);
		monitor.done();
	} 
	
	private static class StreamListener implements IStreamListener {
		private PrintStream out;

		public StreamListener(PrintStream out) {
			this.out = out;
		}

		@Override
		public void streamAppended(String text, IStreamMonitor monitor) {
			out.append(text);
		}
	}

	/**
	 * Produces a command-line script corresponding to the given 
	 * {@link VMRunnerConfiguration}.  Useful for debugging.
	 */
	protected String configScript(VMRunnerConfiguration vmConfig) {
		StringBuilder sb = new StringBuilder();
				
		String[] cp = vmConfig.getClassPath();
		if (cp.length > 0) {
			sb.append("export CLASSPATH=\\\n");
			for (String cpEntry : cp) {
				sb.append(cpEntry);
				sb.append(":\\\n");
			}
			sb.append('\n');
		}
		
		sb.append("cd ");
		sb.append(vmConfig.getWorkingDirectory());
		sb.append('\n');

		sb.append("java ");
		sb.append(vmConfig.getClassToLaunch());
		sb.append(" \\\n");
		String[] args = vmConfig.getProgramArguments();
		for (String arg : args) {
			sb.append(arg);
			sb.append(' ');
			if (!arg.startsWith("-")) {
				sb.append("\\\n");
			}
		}
		return sb.toString();
	}
	
}
