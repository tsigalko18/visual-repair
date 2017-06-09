package edu.illinois.reassert.reflect;

import java.io.File;

import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.cu.SourcePosition;

/**
 * Adjusts the line number of a given {@link SourcePosition} 
 */
public class OffsetPosition implements SourcePosition {
	private final SourcePosition position;
	private final int startOffset;
	private final int endOffset;

	public OffsetPosition(SourcePosition position, int startOffset, int endOffset) {
		this.position = position;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
	}

	public SourcePosition getWrapped() {
		SourcePosition wrapped = this.position;	
		if (wrapped instanceof OffsetPosition) {
			return ((OffsetPosition) wrapped).getWrapped();
		}
		return wrapped;
	}
	
	@Override
	public int getColumn() {
		return position.getColumn();
	}

	@Override
	public CompilationUnit getCompilationUnit() {
		return position.getCompilationUnit();
	}

	@Override
	public int getEndColumn() {
		return position.getEndColumn();
	}

	@Override
	public int getEndLine() {
		return endOffset + position.getEndLine();
	}

	@Override
	public File getFile() {
		return position.getFile();
	}

	@Override
	public int getLine() {
		return startOffset + position.getLine();
	}

	@Override
	public int getSourceEnd() {
		// currently don't need to adjust this. may in the future.
		return position.getSourceEnd();
	}

	@Override
	public int getSourceStart() {
		// currently don't need to adjust this. may in the future.
		return position.getSourceStart();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getFile().getAbsolutePath());
		int line = getLine();
		if (line >= 1) {
			sb.append(':');
			sb.append(line);
		}
		return sb.toString();
	}
}