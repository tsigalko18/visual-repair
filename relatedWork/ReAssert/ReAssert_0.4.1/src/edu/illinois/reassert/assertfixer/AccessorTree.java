package edu.illinois.reassert.assertfixer;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import spoon.reflect.Factory;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import edu.illinois.reassert.reflect.AssertFactory;

/**
 * Represents a merged spanning tree of two object graphs: one for an
 * "expected" object and the other for an "actual" object.
 * Nodes correspond to objects in the graph and edges correspond
 * to the objects' accessor methods.  
 * <br /><br /> 
 * The tree is used to build a tree of assertions.  
 * <br /><br />
 * Tree invariants are the following:
 * <ul>
 * <li>In internal nodes, the actual and expected values differ 
 *     ({@link AccessorTreeNode#valuesAreEqual()} returns <code>false</code>).</li>
 * <li>In leaf nodes:
 *     <ul>
 *         <li>The expected and actual values are equal 
 *             ({@link AccessorTreeNode#valuesAreEqual()} returns <code>true</code>)</li>
 *         <li>OR both the expected and actual values can be literal values 
 *         	   (see {@link #canBeLiteral(Class)})</li>
 *         <li>OR both the expected and actual values have appeared higher in the 
 *             leaf's branch ({@link Accessor#isBackEdge()} returns true).
 *             This avoids a tree of infinite depth in the case of circular object 
 *             graphs.</li>
 *         <li>OR the tree has reached its maximum depth</li>
 *     </ul>
 * </li>
 * <li>If a value at a node was not produced by an accessor, then {@link #NO_ACCESSOR} is used as a placeholder.</li>
 * <li>If no value was produced at a node, then {@link #NO_VALUE} is used as a placeholder.</li>
 * </ul>
 *      
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class AccessorTree {

	/** 
	 * Null object used when an accessor does not exist and cannot return a value.
	 * Uses a special object since accessors may return <code>null</code>
	 */
	public static final Object NO_VALUE = new Object();	
	/**
	 * Null object used when an accessor does not exist.
	 * Uses a special method that will not match any normal method
	 * found in a user's code.
	 */
	public static final Method NO_ACCESSOR = NULL_ACCESSOR();
	private static Method NULL_ACCESSOR() {
		try {
			return AccessorTree.class.getDeclaredMethod("NULL_ACCESSOR");
		} catch (SecurityException e) {
			throw new RuntimeException(e); 
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e); 
		}
	}

	/**
	 * These are some accessors that should be ignored when building
	 * the accessor tree.
	 * 
	 * @see #isAccessor(Method)
	 */
	private static final Set<String> IGNORED_ACCESSOR_NAMES = 
		new HashSet<String>(Arrays.asList(new String[] {
				"clone", 
				"hashCode",
				"next",
				"previous",
				"prev",
				"iterator",
		})) ;
	
	private AccessorTreeNode root;
	private Set<Object> knownObjects = new HashSet<Object>();
	
	public AccessorTreeNode getRoot() {
		return root;
	}

	/**
	 * @return an accessor tree for the given objects
	 */
	public static AccessorTree build(Object expected, Object actual) {
		return build(expected, actual, -1);
	}
	
	/**
	 * @param maxDepth The maximum depth of the tree. 
	 * If maxDepth < 0, then depth is unbounded.
	 * @return an accessor tree for the given objects.
	 */
	public static AccessorTree build(Object expected, Object actual, int maxDepth) {
		AccessorTree tree = new AccessorTree();
		tree.root = tree.buildNodeTree(
				NO_ACCESSOR, expected, 
				NO_ACCESSOR, actual,
				maxDepth);
		return tree;
	}
	
	private AccessorTreeNode buildNodeTree(
			Method expectedAccessor, 
			Object expectedValue, 
			Method actualAccessor, 
			Object actualValue,
			int depth) {
		AccessorTreeNode node = new AccessorTreeNode();
		node.expected = new Accessor(expectedAccessor, expectedValue);
		node.actual = new Accessor(actualAccessor, actualValue);

		if (knownObjects.contains(expectedValue)) {
			node.expected.isBackEdge = true;
		}
		else {
			knownObjects.add(expectedValue);
		}
		if (knownObjects.contains(actualValue)) {
			node.actual.isBackEdge = true;
		}
		else {
			knownObjects.add(actualValue);
		}
		
		if (depth != 0 && !node.valuesAreEqual() && (!node.expected.isBackEdge || !node.actual.isBackEdge)) { 
			// then recurse over accessors
			
			List<Method> expectedAccessors = findAccessors(expectedValue);
			List<Method> actualAccessors = findAccessors(actualValue);
			List<Method[]> matchedAccessors = findMatchingAccessors(
					expectedAccessors, actualAccessors);
			
			for (Method[] matchedPair : matchedAccessors) {
				Method childExpectedAccessor = matchedPair[0];
				Object childExpectedValue = getAccessorValue(expectedValue, childExpectedAccessor);
				Method childActualAccessor = matchedPair[1];
				Object childActualValue = getAccessorValue(actualValue, childActualAccessor);
				
				AccessorTreeNode childNode = buildNodeTree(
						childExpectedAccessor, 
						childExpectedValue, 
						childActualAccessor, 
						childActualValue,
						depth - 1);
				node.addChild(childNode);
				
			}
		}

		return node;
	}

	private Object getAccessorValue(
			Object target,
			Method accessor) {
		if (target == null || target == NO_VALUE || accessor == NO_ACCESSOR) {
			return NO_VALUE;
		}
		try {
			return accessor.invoke(target); 
		} 
		catch (Throwable e) {
			// just ignore the method if it throws anything
			return NO_VALUE;
		}
	}

	private List<Method[]> findMatchingAccessors(
			List<Method> expectedAccessors,
			List<Method> actualAccessors) {
		List<Method[]> matchedAccessors = new LinkedList<Method[]>(); // HACK: list of method pairs
		for (Method expectedAccessor : expectedAccessors) {
			matchedAccessors.add(new Method[] {expectedAccessor, NO_ACCESSOR} ); 
		}
		// O(n * m), but objects usually have only a handful of accessors
		for (Method actualAccessor : actualAccessors) {
			boolean matched = false;
			for (Method[] match : matchedAccessors) {
				Method expectedAccessor = match[0];
				if (expectedAccessor.getName().equals(actualAccessor.getName())) {
					match[1] = actualAccessor;
					matched = true;
					break;
				}
			}
			if (!matched) {
				matchedAccessors.add(new Method[] {NO_ACCESSOR, actualAccessor});
			}
		}
		return matchedAccessors;
	}
	
	private List<Method> findAccessors(Object object) {
		List<Method> accessors = new LinkedList<Method>();
		if (object == null || object == NO_VALUE || canBeLiteral(object.getClass())) {
			return accessors;
		}
		for (Method method : findPublicSupertype(object.getClass()).getMethods()) {
			if (isAccessor(method)) {
				accessors.add(method);
			}
		}
		return accessors;
	}
	
	private boolean isAccessor(Method method) {
		// TODO: more heuristics: method purity, etc.
		return true
			&& Modifier.isPublic(method.getModifiers()) 
			&& !Modifier.isStatic(method.getModifiers())
			&& !Modifier.isNative(method.getModifiers())
			&& method.getParameterTypes().length == 0
			&& method.getReturnType() != Void.TYPE
			&& method.getDeclaringClass() != Object.class
			&& !IGNORED_ACCESSOR_NAMES.contains(method.getName()) // HACK
			&& method.getAnnotation(Deprecated.class) == null
			;
	}

	public Iterable<AccessorTreeNode> preOrder() {
		return new Iterable<AccessorTreeNode>() {
			@Override
			public Iterator<AccessorTreeNode> iterator() {
				return new PreOrderIterator();
			}
		};
	}	
	
	private class PreOrderIterator implements Iterator<AccessorTreeNode>{
		private Deque<AccessorTreeNode> stack = new LinkedList<AccessorTreeNode>();
		
		public PreOrderIterator() {
			stack.push(root);
		}
		
		@Override
		public boolean hasNext() {
			return stack.size() != 0;
		}

		@Override
		public AccessorTreeNode next() {
			if (!hasNext()) {
				throw new IllegalStateException();
			}
			AccessorTreeNode next = stack.pop();
			for (AccessorTreeNode child : next.children) {
				stack.push(child);
			}
			return next;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		buildString(sb, "", '\t', root);
		return sb.toString();
	}
	
	private void buildString(StringBuilder sb, String prefix, char prefixChar, AccessorTreeNode node) {
		sb.append(prefix).append(node).append('\n');
		for (AccessorTreeNode child : node.getChildren()) {
			buildString(sb, prefix + prefixChar, prefixChar, child);
		}
	}

	public static class AccessorTreeNode {
		private Accessor expected = new Accessor();
		private Accessor actual = new Accessor();
		
		private List<AccessorTreeNode> children = new LinkedList<AccessorTreeNode>();		
				
		public Accessor getExpected() {
			return expected;
		}
		
		public Accessor getActual() {
			return actual;
		}

		public void addChild(AccessorTreeNode childNode) {
			children.add(childNode);
		}
		
		public Iterable<AccessorTreeNode> getChildren() {
			return children;
		}
		
		public boolean hasChildren() {
			return children.size() > 0;
		}
		
		/**
		 * @return <code>true</code> if the expected and actual objects are equal
		 */
		public boolean valuesAreEqual() {
			if (!expected.hasValue() || !actual.hasValue()) {
				return false;
			}
			Object expectedValue = expected.getValue();
			Object actualValue = actual.getValue();
			return 
				expectedValue == actualValue
				|| (expectedValue != null && expectedValue.equals(actualValue));
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(expected);
			sb.append(", ");
			sb.append(actual);
			return sb.toString();
		}
	}
	
	public static class Accessor {
		private Method method = NO_ACCESSOR;
		private Object value = NO_VALUE;
		private boolean isBackEdge = false;
		
		/**
		 * Null object constructor
		 */
		public Accessor() {}
		
		public Accessor(Method method, Object value) {
			this.method = method;
			this.value = value;
		}

		public boolean hasMethod() {
			return method != NO_ACCESSOR;
		}

		/**
		 * @return the accessor method that produced the value accessible through {@link #getValue()}
		 */
		public Method getMethod() {
			return method;
		}
		
		public boolean hasValue() {
			return value != NO_VALUE;
		}
		
		/**
		 * @return the value returned from the accessor when executed on an instance
		 */
		public Object getValue() {
			return value;
		}

		public boolean isBackEdge() {
			return isBackEdge ;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (method == NO_ACCESSOR) {
				sb.append("<NO_ACCESSOR>");
			}
			else {
				sb.append(method.toString());
			}
			sb.append(" = ");
			if (value == NO_VALUE) {
				sb.append("<NO_VALUE>");
			}
			else {
				sb.append(value);
			}
			return sb.toString();
		}

	}

	private Map<Object, CtVariableAccess<?>> knownVariables = null;
	private int objectCounter = 0;		
	private Factory factory = null;
	private AssertFactory assertFactory = null;
	
	/**
	 * Create a tree of assertions out of this tree 
	 */
	public CtStatement buildAssertionTree (
			Factory factory,
			AssertFactory assertFactory,
			CtExpression<?> expectedArg, 
			CtExpression<?> actualArg) {

		this.factory = factory; 
		this.assertFactory = assertFactory;
		knownVariables = new HashMap<Object, CtVariableAccess<?>>();
		AccessorTreeNode root = getRoot();
		if (expectedArg instanceof CtVariableAccess) {
			knownVariables.put(
					root.getExpected().getValue(), 
					((CtVariableAccess<?>)expectedArg));
		}
		if (actualArg instanceof CtVariableAccess) {
			knownVariables.put(
					root.getActual().getValue(), 
					((CtVariableAccess<?>)actualArg));
		}		

		return buildAssertionTree(root,	expectedArg, actualArg);
	}
	
	private CtStatement buildAssertionTree(
			AccessorTreeNode node, 
			CtExpression<?> curExpectedTarget, 
			CtExpression<?> curActualTarget) {
		Accessor expected = node.getExpected();
		Accessor actual = node.getActual();
		if (node.valuesAreEqual()) {
			return createAccessorAssertEquals(
					curExpectedTarget, expected,
					curActualTarget, actual);
		}
		if (actual.getValue() == null) {
			CtExpression<?> accessorArg;
			if (actual.hasMethod()) { // TODO: this if/else appears several places. Refactor.
				accessorArg = createAccessorInvocation(curActualTarget, actual);
			}
			else {
				accessorArg = curActualTarget; 
			}
			return assertFactory.createAssertNull(accessorArg);
		}
		if (actual.getValue() instanceof Boolean) {
			CtExpression<?> accessorArg;
			if (actual.hasMethod()) {
				accessorArg = createAccessorInvocation(curActualTarget, actual);
			}
			else {
				accessorArg = curActualTarget; 
			}
			Boolean value = (Boolean) actual.getValue();
			if (value) {
				return assertFactory.createAssertTrue(accessorArg);
			}
			else {
				return assertFactory.createAssertFalse(accessorArg);
			}
		}
		if (canBeLiteral(actual.getValue())) {
			return createLiteralAssertEquals(curActualTarget, actual);
		}
		if (actual.isBackEdge()) {
			if (knownVariables.containsKey(actual.getValue())) {
				return createKnownVariableAssertEquals(
						knownVariables.get(actual.getValue()), 
						curActualTarget, actual);
			}
			// assert against the value elsewhere in the tree
			return null;
		}
		if (node.hasChildren()){				
			CtBlock<?> container = factory.Core().createBlock();			
			
			CtLocalVariable<?> dummyExpected = 
				createDummyVariableIfNeeded(curExpectedTarget, expected);
			if (dummyExpected != null) {
				container.getStatements().add(dummyExpected);
				CtVariableAccess<?> dummyExpectedAccess = 
					factory.Code().createVariableAccess(dummyExpected.getReference(), false);
				knownVariables.put(expected.getValue(), dummyExpectedAccess);
				curExpectedTarget =	dummyExpectedAccess;
			}
			
			CtLocalVariable<?> dummyActual = 
				createDummyVariableIfNeeded(curActualTarget, actual);
			if (dummyActual != null) {
				container.getStatements().add(dummyActual);
				CtVariableAccess<?> dummyActualAccess = 
					factory.Code().createVariableAccess(dummyActual.getReference(), false);
				knownVariables.put(actual.getValue(), dummyActualAccess);
				curActualTarget = dummyActualAccess;
			}
			
			for (AccessorTreeNode child : node.getChildren()) {
				Accessor childActual = child.getActual();
				if (childActual.hasMethod()) {
					// recurse only if there is an actual side. We don't care about expected
					CtStatement childStatement = 
						buildAssertionTree(
								child, 
								curExpectedTarget, 
								curActualTarget);
					if (childStatement != null) {
						container.getStatements().add(childStatement);
					}
				}
			}
			
			// make sure variables only exist down the current branch
			if (dummyActual != null) {
				knownVariables.remove(actual.getValue());
			}
			if (dummyExpected != null) {
				knownVariables.remove(expected.getValue());
			}
			
			return container;
		}
		else if (Object.class.equals(findPublicSupertype(actual.getValue().getClass()))) {
			// Can only assert that an Object is not null 
			CtExpression<?> accessorArg;
			if (actual.hasMethod()) {
				accessorArg = createAccessorInvocation(curActualTarget, actual);
			}
			else {
				accessorArg = curActualTarget; 
			}
			return assertFactory.createAssertNotNull(accessorArg);
		}
		return null;
	}

	private CtStatement createLiteralAssertEquals(
			CtExpression<?> target, 
			Accessor accessor) { 
		CtExpression<?> literalArg = 
			createLiteral(accessor.getValue());
		CtExpression<?> accessorArg;
		if (accessor.hasMethod()) {
			accessorArg = createAccessorInvocation(target, accessor);
		}
		else {
			accessorArg = target; 
		}
		
		return assertFactory.createAssertEquals(literalArg, accessorArg);
	}

	private CtExpression<?> createLiteral(Object value) {
		assert value != null; 
		assert canBeLiteral(value);

		if (value instanceof Class) {
			// literal .class
			return factory.Code().createClassAccess(
					factory.Class().createReference((Class<?>)value));
		}
		if (value.getClass().isArray()) {
			return createArrayLiteral(value);
		}
		return factory.Code().createLiteral(value);
	}

	@SuppressWarnings("unchecked")
	private CtNewArray<?> createArrayLiteral(Object value) {
		assert value.getClass().isArray();
		
		CtNewArray toReturn = 
			factory.Core().createNewArray();
		toReturn.setType(
				factory.Type().createReference(value.getClass()));
		toReturn.setElements(
				createArrayElements(value));
		return toReturn;
	}

	private List<CtExpression<?>> createArrayElements(Object actualValue) {
		List<CtExpression<?>> elements = new LinkedList<CtExpression<?>>();
		int length = Array.getLength(actualValue);
		for (int i = 0; i < length; i++) {
			// get array values by reflection because we can't 
			// cast primitive arrays to Object[]
			Object element = Array.get(actualValue, i);
			// recurse through multidimensional array
			elements.add(createLiteral(element));
		}
		return elements;
	}

	private CtStatement createKnownVariableAssertEquals(
			CtVariableAccess<?> knownVariableArg, 
			CtExpression<?> target, 
			Accessor accessor) {
		CtExpression<?> accessorArg = 
			createAccessorInvocation(target, accessor);
		
		return assertFactory.createAssertEquals(knownVariableArg, accessorArg);
	}
	
	private CtStatement createAccessorAssertEquals(
			CtExpression<?> expectedTarget,
			Accessor expected,
			CtExpression<?> actualTarget,
			Accessor actual) {
		CtExpression<?> expectedArg = 
			createAccessorInvocation(expectedTarget, expected);
		CtExpression<?> actualArg = 
			createAccessorInvocation(actualTarget, actual);

		return assertFactory.createAssertEquals(expectedArg, actualArg);
	}

	private CtExpression<?> createAccessorInvocation(
			CtExpression<?> target,
			Accessor accessor) {
		assert accessor.hasMethod();
		
		CtExecutableReference<?> accessorRef = 
			factory.Method().createReference(accessor.getMethod());
		CtExpression<?> invocation = 
			factory.Code().createInvocation(target, accessorRef);
		return invocation;
	}

	private CtLocalVariable<?> createDummyVariableIfNeeded(
			CtExpression<?> target,
			Accessor accessor) {
		if (target == null || accessor.getValue() == null || !accessor.hasValue()) {
			return null;
		}
		Class<?> valueClass = accessor.getValue().getClass();
		CtTypeReference<?> valueTypeRef = findPublicSupertype(factory.Type().createReference(valueClass));
		if (!(target instanceof CtVariableAccess)) {
			// extract expression to temporary variable to keep it from
			// being executed multiple times (once per assertion)
			return createDummyVariable(valueTypeRef, target);
		}		
		if (accessor.hasMethod()) {
			// extract accessor invocation to temporary variable even
			// though it should be side-effect free
			return createDummyAccessorInvocation(target, accessor);
		}
		if (!valueTypeRef.isAssignableFrom(target.getType())) {
			// cast the target to the correct type and store to 
			// temporary variable so subsequent accessor invocations compile
			return createDummyVariable(valueTypeRef, target);
		}
		return null;
	}

	/**
	 * Find first public, non-anonymous type in hierarchy
	 */
	private CtTypeReference<?> findPublicSupertype(CtTypeReference<?> typeRef) {		
		Class<?> type = findPublicSupertype(typeRef.getActualClass());
		return factory.Type().createReference(type);
	}

	/**
	 * Find first public, non-anonymous type in hierarchy
	 */
	private Class<?> findPublicSupertype(Class<?> type) {
		while(type.isAnonymousClass() 
				|| !(Modifier.isPublic(type.getModifiers()))) {
			type = type.getSuperclass();
		}
		return type;
	}
	
	private CtLocalVariable<?> createDummyAccessorInvocation(
			CtExpression<?> target,
			Accessor accessor) {
		CtExpression<?> initExpression = createAccessorInvocation(target, accessor);
		return createDummyVariable(accessor.getValue().getClass(), initExpression);
	}

	private CtLocalVariable<?> createDummyVariable(
			Class<?> valueType, 
			CtExpression<?> initExpression) {
		CtTypeReference<?> variableTypeRef = 
			factory.Type().createReference(valueType);
		return createDummyVariable(variableTypeRef, initExpression);
	}

	@SuppressWarnings("unchecked")
	private CtLocalVariable<?> createDummyVariable(
			CtTypeReference<?> variableTypeRef, 
			CtExpression initExpression) {
		variableTypeRef = findPublicSupertype(variableTypeRef);
		if (!variableTypeRef.isAssignableFrom(initExpression.getType())) {
			initExpression.getTypeCasts().add(variableTypeRef);
		}
		String varName = variableTypeRef.getSimpleName().toLowerCase() + (objectCounter ++);
		CtLocalVariable<?> variable = 
			factory.Code().createLocalVariable(
					variableTypeRef, varName, initExpression);
		
		return variable;
	}
	
	private boolean canBeLiteral(Object value) {
		return value == null || canBeLiteral(value.getClass());
	}
	private boolean canBeLiteral(Class<?> actualClass) {		
		while(actualClass.isArray()) {
			actualClass = actualClass.getComponentType();
		}
		return 
			actualClass.isPrimitive()
			|| Boolean.class.isAssignableFrom(actualClass)
			|| Byte.class.isAssignableFrom(actualClass)
			|| Short.class.isAssignableFrom(actualClass)
			|| Integer.class.isAssignableFrom(actualClass)
			|| Long.class.isAssignableFrom(actualClass)
			|| Float.class.isAssignableFrom(actualClass)
			|| Double.class.isAssignableFrom(actualClass)
			|| Character.class.isAssignableFrom(actualClass)
			|| String.class.isAssignableFrom(actualClass)
			|| Class.class.isAssignableFrom(actualClass);
	}

	
}
