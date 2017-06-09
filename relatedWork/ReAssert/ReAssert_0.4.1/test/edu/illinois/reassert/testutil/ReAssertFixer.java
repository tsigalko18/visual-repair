package edu.illinois.reassert.testutil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.commons.io.FileUtils;

import spoon.reflect.code.CtBlock;
import spoon.reflect.cu.SourceCodeFragment;
import spoon.reflect.declaration.CtExecutable;
import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.FixResult;
import edu.illinois.reassert.FixStrategy;
import edu.illinois.reassert.FixStrategyBase;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.reflect.Factory;
import edu.illinois.reassert.reflect.ParentVisitor;
import edu.illinois.reassert.reflect.SimpleSpoonLoader;

/**
 * {@link FixStrategy} that repairs ReAssert's own tests 
 * (i.e. those that use {@link FixChecker}) by replacing the
 * the method annotated with @{@link Fix}.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class ReAssertFixer extends FixStrategyBase {

	public ReAssertFixer(Factory factory) {
		super(factory);
	}

	@Override
	public FixResult fix(
			Method testMethod,
			Throwable failureException)
			throws UnfixableException {
		if (!(failureException instanceof FixCheckerComparisonFailure)) {
			return null;	
		}
		FixCheckerComparisonFailure record = (FixCheckerComparisonFailure) failureException;
		
		// @Test method
		CtExecutable<?> actual = record.getActualMethod();
		// @Fix method
		CtExecutable<?> expected = getFromCurrentFactory(record.getExpectedMethod());
		CtBlock<?> newBody = replaceFixBody(actual, expected);
		SourceCodeFragment frag = 
			getFactory().Fragment().replace(expected.getBody(), newBody);		
		
		final File source = expected.getPosition().getFile();
		final File backup = new File(source.getPath() + ".bak");
		
		// Order matters here. 
		// First, create the result first to build the snippet
		CodeFixResult result = new CodeFixResult(newBody, frag) {
			@Override
			public File save(File outDir, String fixFileSuffix)
					throws IOException {
				// Third, restore the source file so comparison works
				if (backup.exists()) {
					FileUtils.copyFile(backup, source);
					backup.delete();
				}
				return super.save(outDir, fixFileSuffix);
			}
		};
		
		// Second, overwrite the source file so the next run sees the fix
		try {
			if (!backup.exists()) {
				FileUtils.copyFile(source, backup);
			}			
			// TODO: refactor
			new SimpleSpoonLoader(getFactory()).output(
					expected.getPosition().getCompilationUnit(), 
					source);
		} 
		catch (IOException e) {
			throw new RuntimeException(e); // TODO: Handle this
		}
		
		return result;
	}

	@SuppressWarnings("unchecked")
	private CtBlock replaceFixBody(CtExecutable actual, CtExecutable expected) {
		CtBlock newBody = actual.getBody();
		newBody.setPosition(expected.getBody().getPosition());
		expected.setBody(newBody);
		new ParentVisitor(expected).scan(newBody);
		return newBody;
	}

	/**
	 * Returns the executable from this strategy's {@link Factory}.
	 * Necessary because the given element's Factory is not the
	 * one in which the fix is applied.  
	 */
	private CtExecutable<?> getFromCurrentFactory(CtExecutable<?> expected) {
		return getFactory().Method().createReference(
					expected.getReference().toString()).getDeclaration();
	}
	
	
	
}
