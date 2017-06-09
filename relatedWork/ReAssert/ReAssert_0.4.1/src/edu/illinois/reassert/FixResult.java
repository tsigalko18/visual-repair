package edu.illinois.reassert;

import java.io.File;
import java.io.IOException;

/**
 * A record of a single successful fix applied by an {@link FixStrategy}
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public abstract class FixResult {

	private FixStrategy appliedFixer;

	public FixStrategy getAppliedFixer() {
		return appliedFixer;
	}

	public void setAppliedFixer(FixStrategy appliedFixer) {
		this.appliedFixer = appliedFixer;
	}

	/**
	 * Save a fix to the file system
	 * @param outDir the directory in which to save the fixed file
	 * @param fixFileSuffix the file extension for the fix
	 * @return the saved file or null if no file changed
	 */
	public abstract File save(File outDir, String fixFileSuffix) throws IOException;

	/**
	 * Save a fix snippet to the file system
	 * @param outDir the directory in which to save the snippet
	 * @param snippetFileSuffix the file extension for the snippet
	 * @return the saved file or null
	 */
	public abstract File saveSnippet(File outDir, String snippetFileSuffix) throws IOException;

}