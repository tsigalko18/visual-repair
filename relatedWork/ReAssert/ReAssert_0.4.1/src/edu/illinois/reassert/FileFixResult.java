package edu.illinois.reassert;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * Represents a {@link FixResult} in which an external file has changed
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class FileFixResult extends FixResult {

	private File fixedFile;

	public FileFixResult(File fixedFile) {
		this.fixedFile = fixedFile;
	}
	
	public File getFixedFile() {
		return fixedFile;
	}
	
	@Override
	public File save(File outDir, String fixFileSuffix) throws IOException {
		File destFile = new File(outDir, getFixedFile().getPath() + fixFileSuffix);
		FileUtils.copyFile(getFixedFile(), destFile);
		return destFile;
	}

	@Override
	public File saveSnippet(File outDir, String snippetFileSuffix) {
		throw new RuntimeException(); // TODO
	}
	
}
