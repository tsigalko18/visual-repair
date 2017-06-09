package edu.illinois.reassert.reflect;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import spoon.support.builder.CtFile;
import spoon.support.builder.CtFolder;
import spoon.support.builder.support.CtVirtualFile;
import spoon.support.builder.support.CtVirtualFolder;

/**
 * Implementation of {@link CtFile} that, unlike {@link CtVirtualFile},
 * defines a path and retains the source as a String.
 * Used for testing.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class VirtualFile implements CtFile {

	private String path;
	private String source;
	private InputStream content;

	public VirtualFile(String path, String source) {
		this.path = path;
		this.source = source;
		this.content = new ByteArrayInputStream(source.getBytes());
	}

	@Override
	public String getPath() {
		return path;
	}

	public String getSource() {
		return source;
	}
	
	@Override
	public InputStream getContent() {
		return content;
	}

	@Override
	public boolean isJava() {
		return true;
	}

	@Override
	public String getName() {
		return getPath();
	}

	@Override
	public CtFolder getParent() {
		return new CtVirtualFolder();
	}

	@Override
	public boolean isFile() {
		return true;
	}
}
