package edu.illinois.reassert.assertfixer;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.cu.SourceCodeFragment;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import edu.illinois.reassert.AssertFixer;
import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.RecordedAssertFailure;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.reflect.AssertFactory;
import edu.illinois.reassert.reflect.Factory;

/**
 * Fixes asserts against calls to size() and isEmpty().
 * Requires multi-step fixing using {@link AssertEqualsExpandAccessorsFixer} and
 * {@link InvertBooleanAssertFixer}.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class AssertCollectionSizeFixer extends AssertFixer {

	public AssertCollectionSizeFixer(Factory factory) {
		super(factory);
	}
	
	@Override
	public CodeFixResult fix(
			Method testMethod, 
			CtInvocation<?> assertion, 
			Throwable failureException)
			throws UnfixableException {
		AssertFactory assertFactory = getAssertFactory(testMethod);
		
		int actualIndex = 1;
		if (assertFactory.isAssertTrue(assertion)) {
			if (assertion.getArguments().size() == 1) {
				actualIndex = 0;
			}
		}
		else if (assertFactory.isAssertEquals(assertion)) {
			if (assertion.getArguments().size() == 3) {
				actualIndex = 2;
			}
		}
		else {
			// can't fix this type of assertion
			return null;
		}
		CtExpression<?> actual = assertion.getArguments().get(actualIndex);
		if (actual instanceof CtInvocation<?>) {
			CtInvocation<?> actualCall = (CtInvocation<?>) actual;		
			CtExpression<?> target = actualCall.getTarget();
			if (isCallTo(actualCall, "isEmpty") 
					&& hasMethod(target, "size")) {
				changeAssertIsEmptyToAssertEqualsSize(assertFactory, assertion, target);
				SourceCodeFragment frag = getFactory().Fragment()
					.replace(assertion, assertion);
				return new CodeFixResult(assertion, frag);
			}
			else if (isCallTo(actualCall, "size")
					&& hasMethod(target, "isEmpty")
					&& failureException instanceof RecordedAssertFailure) {
				RecordedAssertFailure record = (RecordedAssertFailure) failureException;
				Object actualValue = record.getArgs()[actualIndex];
				if (new Long(0).equals(actualValue) 
						|| new Integer(0).equals(actualValue)) {
					changeAssertSizeZeroToAssertEmpty(assertFactory, assertion, target);
					SourceCodeFragment frag = getFactory().Fragment()
						.replace(assertion, assertion);
					return new CodeFixResult(assertion, frag);
				}
			}
		}
		return null;
	}
	
	private boolean hasMethod(CtExpression<?> target, String methodName) {
		if (target == null) {
			return false;
		}
		return hasMethod(target.getType(), methodName);
	}

	private boolean hasMethod(CtTypeReference<?> targetType, String methodName) {
		// check this type and supertypes
		Collection<CtExecutableReference<?>> methods = targetType.getAllExecutables();
		for (CtExecutableReference<?> method : methods) {
			if (method.getModifiers().contains(ModifierKind.PUBLIC)
					&& method.getParameterTypes().size() == 0
					&& method.getSimpleName().equals(methodName)) {
				return true;
			}
		}
		// check superinterfaces 
		Set<CtTypeReference<?>> superInterfaces = targetType.getSuperInterfaces();
		for (CtTypeReference<?> superInterface : superInterfaces) {
			if (hasMethod(superInterface, methodName)) {
				return true;
			}
		}
		return false;
	}

	private boolean isCallTo(
			CtInvocation<?> call,
			String methodName) {
		CtExecutableReference<?> executable = call.getExecutable();
		return executable.getParameterTypes().size() == 0 
			&& methodName.equals(executable.getSimpleName());
	}

	@SuppressWarnings("unchecked")
	private void changeAssertSizeZeroToAssertEmpty(
			AssertFactory assertFactory,
			CtInvocation<?> assertion,
			CtExpression<?> target) {		
		CtExecutableReference assertTrue = assertFactory.getAssertTrueReference();
		assertion.setExecutable(assertTrue);
		CtInvocation<?> isEmptyInv = 
			getFactory().Code().createInvocation(target, 
					getFactory().Executable().createReference(
					"void java.util.Collection#isEmpty()"));
		assertion.getArguments().clear();
		assertion.getArguments().add(isEmptyInv);
	}
	
	@SuppressWarnings("unchecked")
	private void changeAssertIsEmptyToAssertEqualsSize(
			AssertFactory assertFactory,
			CtInvocation<?> assertion,
			CtExpression<?> target) {
		CtExecutableReference assertEquals = assertFactory.getAssertEqualsReference();
		assertion.setExecutable(assertEquals);
		// set expected to an obviously incorrect value and let the assertEquals fixer
		// handle figuring out the correct value.
		CtLiteral<Integer> expected = getFactory().Code().createLiteral(-1);
		CtInvocation<?> sizeInv = 
			getFactory().Code().createInvocation(target, 
					getFactory().Executable().createReference(
					"int java.util.Collection#size()"));
		assertion.getArguments().clear();
		assertion.getArguments().add(expected);
		assertion.getArguments().add(sizeInv);
	}

}
