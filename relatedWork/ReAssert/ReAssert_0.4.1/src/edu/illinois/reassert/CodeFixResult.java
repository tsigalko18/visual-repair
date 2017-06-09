package edu.illinois.reassert;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.cu.SourceCodeFragment;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import edu.illinois.reassert.reflect.SimpleSpoonLoader;

/**
 * Represents a {@link FixResult} in which code has changed
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class CodeFixResult extends FixResult {

	private CtElement fixedElement;
	private SimpleSpoonLoader loader;
	private CompilationUnit fixedCU;
	private SourceCodeFragment fragment;
	
	public CodeFixResult(CtElement fixedElement, SourceCodeFragment fragment) {
		SourcePosition fixPosition = fixedElement.getPosition();
		if (fixPosition == null) {
			throw new IllegalArgumentException("Fixed element position is not set");
		}
		this.fixedElement = fixedElement;
		this.fixedCU = fixPosition.getCompilationUnit();
		this.fragment = fragment;
	}

	public CtElement getFixedElement() {
		return fixedElement;
	}

	public void setLoader(SimpleSpoonLoader loader) {
		this.loader = loader;
	}
	
	@Override
	public File save(File outDir, String fixFileSuffix) throws IOException {
		File fixFile = new File(outDir, fixedCU.getFile().getPath() + fixFileSuffix);
		loader.output(fixedCU, fixFile);
		return fixFile;
	}

	@Override
	public File saveSnippet(File outDir, String snippetFileSuffix) throws IOException {
		String fileName = String.format("%s.%d.%d%s", 
				fixedCU.getFile().getPath(),
				fragment.position,
				fragment.replacementLength,
				snippetFileSuffix);
		File fixFile = new File(outDir, fileName);
		FileUtils.writeStringToFile(fixFile, fragment.code);
		return fixFile;
	}
	
}
