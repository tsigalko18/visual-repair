package edu.illinois.reassert.assertfixer;

import java.lang.reflect.Method;
import java.util.List;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.cu.SourceCodeFragment;
import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.FixResult;
import edu.illinois.reassert.FixStrategyBase;
import edu.illinois.reassert.RecordedAssertFailure;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.reflect.Factory;
import edu.illinois.reassert.reflect.ReAssertPrettyPrinter;

/**
 * Strategy that removes any failing assertions introduced by 
 * {@link AssertEqualsExpandAccessorsFixer}.  Prevents nondeterministic
 * assertions from causing a test to fail.  
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class RemoveNondeterministicFixer extends FixStrategyBase {


	public RemoveNondeterministicFixer(Factory factory) {
		super(factory);
	}

	@Override
	public FixResult fix(
			Method testMethod, 
			Throwable failureException)
			throws UnfixableException {		
		if (!(failureException instanceof RecordedAssertFailure)) {
			return null;
		}
		RecordedAssertFailure record = (RecordedAssertFailure) failureException;
		StackTraceElement location = record.getStackTrace()[1]; // one above instrumented
		
		CtBlock<?> failingBlock = findFailingElement(CtBlock.class, location);
		if (failingBlock == null) {
			return null;
		}
		CtStatement failingStatement = findFailingStatement(failingBlock, location);
		if (failingStatement == null) {
			return null;
		}
		removeFailingStatement(failingStatement);
		if (isEmpty(failingBlock)) {
			throw new UnfixableException("No deterministic accessors");
		}

		SourceCodeFragment frag = getFactory().Fragment().modify(failingBlock);
		return new CodeFixResult(failingBlock, frag);
	}

	private void removeFailingStatement(CtStatement failingStatement) {
		CtBlock<?> parentBlock = failingStatement.getParent(CtBlock.class);
		if (parentBlock == null) {
			return;
		}
		parentBlock.getStatements().remove(failingStatement);
		if (isEmpty(parentBlock)) {
			removeFailingStatement(parentBlock);
		}
	}

	private static class LineCounter {
		int line = 0;
		public LineCounter(int startLine) {
			this.line = startLine;
		}
	}
	private CtStatement findFailingStatement(
			CtBlock<?> failingBlock,
			StackTraceElement location) {
		return findFailingStatement(
				failingBlock, 
				new LineCounter(failingBlock.getPosition().getLine()),
				location.getLineNumber());
	}
	
	private CtStatement findFailingStatement(
			CtStatement curStatement,
			LineCounter curLine, 
			final int searchLine) {
		CtStatement found = null;
		// double-dispatch would be nice here, but it would require a HUGE CtVisitor
		if (curStatement instanceof CtBlock) {
			curLine.line++; // opening brace
			for (CtStatement child : ((CtBlock<?>) curStatement).getStatements()) {
				found = findFailingStatement(child, curLine, searchLine);
				if (found != null) {
					break;
				}
			}
			curLine.line++; // closing brace
		}
		else if (curStatement instanceof CtTry) {
			CtTry curTry = (CtTry) curStatement;
			found = findFailingStatement(curTry.getBody(), curLine, searchLine);
			assert found == null; // shouldn't remove anything from try block
			found = findFailingStatement(
					curTry.getCatchers().get(0).getBody(), // ReAssert only makes one catch 
					curLine, searchLine);
		}
		else if (curLine.line == searchLine 
				&& curStatement.getPosition() == null /* to be sure it's ReAssert's */) {
			found = curStatement;
		}
		else {
			curLine.line += lineLength(curStatement);
		}
		
		return found;
	}

	/**
	 * @return true if the block contains no statements or only a dummy variable
	 */
	private boolean isEmpty(CtBlock<?> block) {
		List<CtStatement> statements = block.getStatements();
		return 
			statements.size() == 0
				|| ( statements.size() == 1 
						&& statements.get(0) instanceof CtLocalVariable);
	}

	private int lineLength(CtStatement stmt) {
		ReAssertPrettyPrinter pp = new ReAssertPrettyPrinter(
				getFactory().getEnvironment());
		String source = pp.print(stmt);
		return source.split("\n").length;
	}

}
