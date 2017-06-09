package edu.illinois.reassert.reflect;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.declaration.CtAnnotationType;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtSimpleType;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

/**
 * A scanner that calculates the imports for a given model.
 */
public class ImportScanner extends CtScanner {

	private Set<CtElement> ignored = new HashSet<CtElement>();
	private Set<CtTypeReference<?>> imports = null;
	
	/**
	 * @return the imports for the given compilation unit
	 */
	public Set<CtTypeReference<?>> makeImports(CompilationUnit cu) {
		imports = new TreeSet<CtTypeReference<?>>();
		for (CtSimpleType<?> type : cu.getDeclaredTypes()) {
			addImport(type.getReference());
			scan(type);
		}
		return imports;
	}
	
	protected <T> boolean addImport(CtTypeReference<T> ref) {		
		return imports.add(ref);
	}
	
	public <T> boolean isImported(CtTypeReference<T> ref) {
		return imports.contains(ref);
	}
	
	@Override
	public void scan(CtElement element) {
		if (element != null 
				&& element.getPosition() != null // not added by ReAssert
				&& !ignored.contains(element)) {
			super.scan(element);
		}
	}
	
	/**
	 * Calculates needed imports for the given field access.
	 */
	@Override
	public <T> void visitCtFieldAccess(CtFieldAccess<T> fieldAccess) {
		enter(fieldAccess);
		scan(fieldAccess.getVariable());
		// scan(fieldAccess.getType());
		scan(fieldAccess.getAnnotations());
		scanReferences(fieldAccess.getTypeCasts());
		scan(fieldAccess.getVariable());
		scan(fieldAccess.getTarget());
		exit(fieldAccess);
	}

	@Override
	public <T> void visitCtFieldReference(CtFieldReference<T> reference) {
		enterReference(reference);
		scan(reference.getDeclaringType());
		// scan(reference.getType());
		exitReference(reference);
	}

	@Override
	public <T> void visitCtExecutableReference(
			CtExecutableReference<T> reference) {
		enterReference(reference);
		scanReferences(reference.getParameterTypes());
		scanReferences(reference.getActualTypeArguments());
		exitReference(reference);
	}
	
	@Override
	public <T> void visitCtTypeReference(CtTypeReference<T> reference) {
		if (!(reference instanceof CtArrayTypeReference)) {
			if (reference.getDeclaringType() == null) {
				addImport(reference);
			} else {
				addImport(reference.getDeclaringType());
			}
		}
		super.visitCtTypeReference(reference);

	}

	@Override
	public <A extends Annotation> void visitCtAnnotationType(
			CtAnnotationType<A> annotationType) {
		addImport(annotationType.getReference());
		super.visitCtAnnotationType(annotationType);
	}

	@Override
	public <T extends Enum<?>> void visitCtEnum(CtEnum<T> ctEnum) {
		addImport(ctEnum.getReference());
		super.visitCtEnum(ctEnum);
	}

	@Override
	public <T> void visitCtInterface(CtInterface<T> intrface) {
		addImport(intrface.getReference());
		for (CtSimpleType<?> t : intrface.getNestedTypes()) {
			addImport(t.getReference());
		}
		super.visitCtInterface(intrface);
	}

	@Override
	public <T> void visitCtClass(CtClass<T> ctClass) {
		addImport(ctClass.getReference());
		for (CtSimpleType<?> t : ctClass.getNestedTypes()) {
			addImport(t.getReference());
		}
		super.visitCtClass(ctClass);
	}
	
	@Override
	public <T> void visitCtInvocation(CtInvocation<T> invocation) {
		if (invocation.getTarget() == null 
				&& invocation.getExecutable().isStatic()) {
			addImport(invocation.getExecutable().getDeclaringType());
		}
		super.visitCtInvocation(invocation);
	}

	/**
	 * Do not import types used in the given element
	 */
	public void ignore(CtElement fixedElement) {
		ignored.add(fixedElement);
	}


}