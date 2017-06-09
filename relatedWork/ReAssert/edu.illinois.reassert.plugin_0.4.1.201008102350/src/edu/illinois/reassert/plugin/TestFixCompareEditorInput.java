package edu.illinois.reassert.plugin;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;

/**
 * A compare editor input that shows the difference between test files and their
 * fixed versions.  A user can make changes to each fixed version and save it 
 * to the workspace. 
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class TestFixCompareEditorInput extends CompareEditorInput {

	private IJavaProject project;
	private IFolder fixFolder;
	private IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();

	public TestFixCompareEditorInput(IJavaProject project) {
		super(initConfiguration());
		this.project = project;
		setDirty(true); // always dirty since the fixed file is different than the original
		setTitle("Confirm proposed fixes");
	}

	protected static CompareConfiguration initConfiguration() {
		CompareConfiguration config = new CompareConfiguration();
		config.setLeftLabel("Original Test File");
		config.setLeftEditable(false);
		config.setRightLabel("Fixed Test File");
		config.setRightEditable(true);
		config.setProperty(CompareConfiguration.IGNORE_WHITESPACE, true);
		return config;
	}

	@Override
	public String getOKButtonLabel() {
		return "C&onfirm";
	}
	
	public void setFixFolder(IFolder fixFolder) {
		this.fixFolder = fixFolder;
	}
	
	@Override
	protected Object prepareInput(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		try {
			project.getResource().refreshLocal(IResource.DEPTH_INFINITE, null);
		} 
		catch (CoreException e) {
			throw new ReAssertException("Unable to refresh project", e);
		}
		DiffNode root = new DiffNode(
				new ResourceNode(project.getResource()), 
				new ResourceNode(fixFolder));
		try {
			for (IResource member : fixFolder.members()) {
				root.add(buildDiffTree(member));
			}
		} 
		catch (CoreException e) {
			throw new ReAssertException(
					"Unable to repair failures. See console output for details.", e); // TODO: Handle this
		}
		
		return root;
	}
	
	private IDiffElement buildDiffTree(IResource member) throws CoreException {	
		IResource projectMember = getCorrespondingResource(member);
		DiffNode node = new DiffNode(new ResourceNode(projectMember), new ResourceNode(member));
		
		// recurse if folder
		if (member instanceof IContainer) {
			IContainer container = (IContainer) member;
			for (IResource childMember : container.members()) {
				node.add(buildDiffTree(childMember));
			}
		}
			
		return node;
	}

	/**
	 * Finds and returns the project resource that corresponds to the 
	 * given ReAssert output resource.
	 * 
	 * For example:
	 * 
	 * /project/.reassert/output/src/some/folder ==> /project/src/some/folder
	 * /project/.reassert/output/src/some/folder/SomeFile.java.fix ==> /project/src/some/folder/SomeFile.java
	 */
	protected IResource getCorrespondingResource(IResource member) {
		IPath reassertOutput = new Path(ReAssertLauncher.DEFAULT_OUTPUT_FOLDER);
		IPath reassertRelativePath = member
				.getProjectRelativePath()
				.removeFirstSegments(reassertOutput.segmentCount());
		IPath projectRelativePath = project.getPath().append(reassertRelativePath);
		if (member instanceof IFile) {
			// TODO: make it such that reassert does not add file extensions
			projectRelativePath = projectRelativePath.removeFileExtension();
		}
		
		return wsroot.findMember(projectRelativePath);
	}

	@Override
	protected void contentsCreated() {
		super.contentsCreated();
		// display first change
		getNavigator().selectChange(true);
	}
	
	@Override
	public void saveChanges(IProgressMonitor monitor) throws CoreException {
		super.saveChanges(monitor); // reflects buffer changes back to resource nodes
		
		DiffNode rootDiff = (DiffNode) getCompareResult();
		saveChanges(rootDiff); // recurses through nodes, saving to the workspace
	}

	/**
	 * Recurse through the given DiffNode tree, saving leaf nodes (IFiles) back to the workspace
	 */
	private void saveChanges(DiffNode node) throws CoreException {
		if (node.hasChildren()) {
			// recurse to leaves
			for (IDiffElement element : node.getChildren()) {
				saveChanges((DiffNode) element);
			}
		}
		else {
			// at leaf, copy reassert fix over to project file
			ResourceNode reassertNode = (ResourceNode) node.getRight();
			ResourceNode projectNode = (ResourceNode) node.getLeft();
			IFile projectFile= ((IFile) projectNode.getResource());
			projectFile.setContents(reassertNode.getContents(), 
					IResource.FORCE | IResource.KEEP_HISTORY, null);
		}
	}

}

