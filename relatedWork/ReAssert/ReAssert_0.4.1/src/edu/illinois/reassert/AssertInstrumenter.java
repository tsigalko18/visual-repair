package edu.illinois.reassert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;

/**
 * Class loader that instruments methods passed to {@link #instrument(Class, String)} 
 * such that if they throw an exception, the exception is wrapped in a 
 * {@link RecordedAssertFailure} which holds the values passed to the method.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class AssertInstrumenter extends org.apache.bcel.util.ClassLoader {

	private static final String[] IGNORED_PACKAGES = new String[] {
		"java.", 
		"javax.", 
		"sun.",
		"org.eclipse.",
		"org.xml.",
		"org.apache.commons.logging.", // class loading breaks logger loading
	};

	public AssertInstrumenter() {
		super(IGNORED_PACKAGES);
	}

	public AssertInstrumenter(ClassLoader deferTo) {
		this(deferTo, IGNORED_PACKAGES);
	}
	
	public AssertInstrumenter(ClassLoader deferTo, String[] ignoredPackages) {
		super(deferTo, ignoredPackages);
	}

	/**
	 * Map class names to the set of method names to instrument.
	 */
	private Map<String, Set<String>> toInstrument = new HashMap<String, Set<String>>();

	public void instrument(Class<?> clazz, String methodName) {
		instrument(clazz.getName(), methodName);
	}

	public void instrument(String className, String methodName) {
		className = className.replace('$', '.');
		if (!toInstrument.containsKey(className)) {
			toInstrument.put(className, new HashSet<String>());
		}
		toInstrument.get(className).add(methodName);
	}
	
	private boolean shouldInstrument(JavaClass clazz) {
		return shouldInstrument(clazz.getClassName());
	}
	
	public boolean shouldInstrument(String className) {
		return getMethodsToInstrument(className) != null;
	}
	
	private boolean shouldInstrument(JavaClass clazz, Method method) {
		String className = clazz.getClassName();
		String methodName = method.getName();
		return shouldInstrument(className, methodName);
	}
	
	public boolean shouldInstrument(String className, String methodName) {		
		Set<String> methodsToInstrument = getMethodsToInstrument(className);
		return methodsToInstrument != null && methodsToInstrument.contains(methodName);
	}

	private Set<String> getMethodsToInstrument(String className) {
		className = className.replace('$', '.');
		return toInstrument.get(className);
	}
	
	@Override
	protected JavaClass modifyClass(JavaClass clazz) {
		if (!shouldInstrument(clazz)) {
			String className = clazz.getClassName();
			if (className.startsWith("edu.illinois.reassert") 
					&& !className.startsWith("edu.illinois.reassert.test.")) {
				// ignore if part of ReAssert except for test classes
				return null;
			}
			if (className.startsWith("org.junit.") 
					|| className.startsWith("junit.framework.")) {
				// ignore JUnit classes unless explicitly told to instrument them
				return null;
			}
			// instrument any referenced classes
			return clazz;
		}
		
		ClassGen classGen = new ClassGen(clazz);
		for (Method method : classGen.getMethods()) {
			if (shouldInstrument(clazz, method)) {				
				String instrumenteeName = method.getName() + "_Original";

				MethodGen instrumentee = new MethodGen(
						method, 
						classGen.getClassName(), 
						classGen.getConstantPool());
				instrumentee.setName(instrumenteeName);				
				classGen.addMethod(instrumentee.getMethod());
								
				MethodGen instrumented = 
					buildInstrumented(classGen, method, instrumenteeName);
				Method newMethod = instrumented.getMethod();
				
				// useful for debugging
//				System.out.println(newMethod);
//				System.out.print(newMethod.getName());
//				System.out.println(newMethod.getSignature());
//				System.out.println(newMethod.getCode());
				
				classGen.replaceMethod(method, newMethod);
			}
		}

		return classGen.getJavaClass();
	}		
	
	private MethodGen buildInstrumented(
			ClassGen classGen, 
			Method method, 
			String instrumenteeName) {
		ConstantPoolGen cp = classGen.getConstantPool();
		Type[] argTypes = method.getArgumentTypes();
		
		InstructionFactory factory = new InstructionFactory(classGen);
		InstructionList il = new InstructionList();
		InstructionHandle startTry = il.append(new NOP());
		
		int localIndexStart = 0;
		if (!method.isStatic()) {
			il.append(InstructionFactory.createThis());
			// skip the slot for the "this" parameter
			localIndexStart = 1;
		}
		
		// load arguments for call to original method
		int localIndex = localIndexStart;
		for (Type argType : argTypes) {
			il.append(InstructionFactory.createLoad(argType, localIndex));
			localIndex += argType.getSize();
		}
		
		short invocationType;
		if (method.isStatic()) {
			invocationType = Constants.INVOKESTATIC;
		}
		else {
			invocationType = Constants.INVOKEVIRTUAL;
		}
		InstructionHandle endTry = 
			il.append(factory.createInvoke(
					classGen.getClassName(),
					instrumenteeName,
					method.getReturnType(),
					method.getArgumentTypes(),
					invocationType));
		
		// branch over catch block
		BranchInstruction branchOverCatch = 
			InstructionFactory.createBranchInstruction(Constants.GOTO, null);
		il.append(branchOverCatch);
		
		// store thrown exception
		InstructionHandle startCatch = 
			il.append(InstructionFactory.createStore(Type.OBJECT, localIndex));
		// create instance of recorder
		il.append(factory.createNew(RecordedAssertFailure.class.getName()));
		il.append(InstructionConstants.DUP);
		// push thrown exception onto stack to pass to recorder
		il.append(InstructionFactory.createLoad(Type.OBJECT, localIndex));
		// push length of varargs array
		il.append(factory.createConstant(argTypes.length));
		// create (one-dimensional) varargs array 
		il.append(factory.createNewArray(Type.OBJECT, (short) 1));

		// varargs[argIndex] = arg[localIndex]
		localIndex = localIndexStart;
		int argIndex = 0;
		for (Type argType : argTypes) {
			// array reference
			il.append(InstructionConstants.DUP);
			// index
			il.append(new PUSH(cp, argIndex++));
			// load argument
			il.append(InstructionFactory.createLoad(argType, localIndex));
			if (argType instanceof BasicType) {
				// box if needed
				il.append(createBoxInstruction(factory, (BasicType) argType));
			}
			// store argument to array
			il.append(InstructionConstants.AASTORE);
			localIndex += argType.getSize();
		}
		// throw recorded exception
		il.append(factory.createInvoke(
				RecordedAssertFailure.class.getName(), 
				"<init>", 
				Type.VOID, 
				new Type[] { 
						new ObjectType(Throwable.class.getName()), 
						new ArrayType(Type.OBJECT, 1) }, 
				Constants.INVOKESPECIAL));
		il.append(InstructionConstants.ATHROW);
		InstructionHandle end = 
			il.append(InstructionFactory.createReturn(Type.VOID));
		branchOverCatch.setTarget(end);

		MethodGen instrumented = new MethodGen(
				method.getAccessFlags(),
				method.getReturnType(),
				method.getArgumentTypes(),
				null,
				method.getName(),
				classGen.getClassName(),			
				il,
				cp);
		instrumented.addExceptionHandler(startTry, endTry, startCatch, 
				new ObjectType(Throwable.class.getName()));
		instrumented.setMaxStack();
		instrumented.setMaxLocals();
		return instrumented;
	}

	private Instruction createBoxInstruction(InstructionFactory factory, BasicType argType) {
		Class<?> boxClass;
		if (argType == Type.BOOLEAN) {
			boxClass = Boolean.class;
		}
		else if (argType == Type.BYTE) {
			boxClass = Byte.class;
		}
		else if (argType == Type.SHORT) {
			boxClass = Short.class;
		}
		else if (argType == Type.CHAR) {
			boxClass = Character.class;
		}
		else if (argType == Type.INT) {
			boxClass = Integer.class;
		}
		else if (argType == Type.LONG) {
			boxClass = Long.class;
		}
		else if (argType == Type.DOUBLE) {
			boxClass = Double.class;
		}
		else if (argType == Type.FLOAT) {
			boxClass = Float.class;
		}
		else {
			throw new IllegalArgumentException("Unknown basic type: " + argType);
		}		
		return factory.createInvoke(
				boxClass.getName(), 
				"valueOf", 
				new ObjectType(boxClass.getName()), 
				new Type[] { argType }, 
				Constants.INVOKESTATIC);
	}

}
