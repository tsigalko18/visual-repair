package edu.illinois.reassert.reflect;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import spoon.processing.Environment;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtSimpleType;
import spoon.reflect.factory.AnnotationFactory;
import spoon.reflect.factory.ClassFactory;
import spoon.reflect.factory.CompilationUnitFactory;
import spoon.reflect.factory.EnumFactory;
import spoon.reflect.factory.InterfaceFactory;
import spoon.reflect.factory.TypeFactory;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.builder.CtResource;
import spoon.support.builder.SpoonBuildingManager;

/**
 * Implementation of {@link spoon.reflect.Factory} that parses files on demand
 * and provides additional factories for producing test code.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class Factory extends spoon.reflect.Factory {

	private static final long serialVersionUID = 1L;
	
	private final AssertFactory junit3Factory = new JUnit3Factory(this);
	private final AssertFactory junit4Factory = new JUnit4Factory(this);
	private OnDemandCompilationUnitFactory cuFactory = new OnDemandCompilationUnitFactory();
	private OnDemandTypeFactory typeFactory = new OnDemandTypeFactory();
	private OnDemandClassFactory classFactory = new OnDemandClassFactory(typeFactory);
	private FragmentFactory fragmentFactory = new FragmentFactory(this);

	private List<String> sourcePaths = new LinkedList<String>();
	private Set<File> builtFiles = new HashSet<File>();


	public Factory() {
		super(new DefaultCoreFactory(), initEnvironment());
	}
	
	private static Environment initEnvironment() {
		StandardEnvironment env = new StandardEnvironment(); 
		env.setComplianceLevel(6);
		env.setVerbose(false);
		env.setDebug(false);
		env.setTabulationSize(5);
		env.useTabulations(true);
		env.useSourceCodeFragments(true);	
		return env;
	}

	/**
	 * Add a path in which to search for source files.
	 */
	public void addSourcePath(String sourcePath) {
		this.sourcePaths.add(sourcePath);
	}
	
	/**
	 * Parse the given resource and add it to the internal model
	 */
	public void addSource(CtResource source) {
		if (source instanceof VirtualFile) {
			cuFactory.virtualFiles.put(source.getPath(), (VirtualFile) source);
		}
		SpoonBuildingManager b = new SpoonBuildingManager(this);
		try {
			b.addInputSource(source);
			b.build();
		}
		catch (Exception e) {
			throw new RuntimeException(e); // TODO: Handle this
		}
	}
	
	/**
	 * Parses the given file and includes its contents in the in-memory model.
	 * Called automatically from on-demand subfactories.  
	 */
	private void build(File toBuild) {
		if (!toBuild.exists() || toBuild.isHidden() || builtFiles.contains(toBuild)) {
			return;
		}
		builtFiles.add(toBuild);
		
		if (toBuild.isDirectory()) {
			for (File child : toBuild.listFiles()) {
				build(child);
			}
		}
		else {
			SpoonBuildingManager b = new SpoonBuildingManager(this);
			try {
				b.addInputSource(toBuild);
				b.build();
			} 
			catch (Exception e) {
				throw new RuntimeException(e); // TODO: handle this
			}
		}
	}
	
	/**
	 * Build any files contained in the given path relative to the source paths
	 * provided to {@link #addSourcePath(String)}
	 * Called automatically from on-demand subfactories.  
	 */
	protected void buildInSourcePaths(String relativePath) {
		for (String sourcePath : sourcePaths) {
			File sourceDir = new File(sourcePath, relativePath);
			if (sourceDir.exists() && sourceDir.isDirectory()) {
				for (File sourceFile : sourceDir.listFiles()) {
					if (sourceFile.isFile()) {
						build(sourceFile);
					}
				}
			}
		}
	}
	
	/**
	 * @return the {@link AssertFactory} that matches the test framework of
	 * the given test method.
	 */
	public AssertFactory Assert(Method testMethod) {
		org.junit.Test testAnn = testMethod.getAnnotation(org.junit.Test.class);
		if (testAnn == null) {
			// method doesn't have @Test annotation, so probably 
			// JUnit 3, but check class to be sure 
			return Assert(testMethod.getDeclaringClass());
		}
		return Assert4();
	}

	/**
	 * @return the {@link AssertFactory} that matches the test framework of
	 * the given test class
	 */
	public AssertFactory Assert(Class<?> testClass) {
		if (junit.framework.Test.class.isAssignableFrom(testClass)) {
			return Assert3();
		}
		return Assert4();
	}

	/**
	 * @return the {@link AssertFactory} for JUnit 4
	 */
	public AssertFactory Assert4() {
		return junit4Factory;
	}

	/**
	 * @return the {@link AssertFactory} for JUnit 3
	 */
	public AssertFactory Assert3() {
		return junit3Factory;
	}
	
	public FragmentFactory Fragment() {
		return fragmentFactory ;
	}
	
	@Override
	public OnDemandCompilationUnitFactory CompilationUnit() {
		return cuFactory;
	}
	
	public class OnDemandCompilationUnitFactory extends CompilationUnitFactory {
		private static final long serialVersionUID = 1L;
		private Map<String, VirtualFile> virtualFiles = new HashMap<String, VirtualFile>();
		private Map<String, CompilationUnit> known = new HashMap<String, CompilationUnit>();

		public OnDemandCompilationUnitFactory() {
			super(Factory.this);
		}

		@Override
		public CompilationUnit create(String filePath) {
			CompilationUnit cu = known.get(filePath);
			if (cu == null) {
				if (virtualFiles.containsKey(filePath)) {
					cu = new VirtualCompilationUnit(virtualFiles.get(filePath));
					cu.setFactory(Factory.this);
				}
				else {
					build(new File(filePath));
					cu = super.create(filePath);
				}			
				known.put(filePath, cu);
			}
			return cu;
		}
		
		/**
		 * Searches the paths provided to {@link Factory#addSourcePath(String)}
		 * in order for the given compilation unit
		 * 
		 * @return the found {@link CompilationUnit} or null
		 */
		public CompilationUnit find(String filePath) {
			for (String sourcePath : sourcePaths) {
				File sourceFile = new File(sourcePath, filePath);
				if (sourceFile.exists()) {
					return create(sourceFile.getPath());
				}
			}
			return null;
		}

		public Set<CompilationUnit> getAll() {
			Set<CompilationUnit> all = new HashSet<CompilationUnit>();
			all.addAll(known.values());
			return all;
		}
	}
	
	@Override
	public OnDemandTypeFactory Type() {
		return typeFactory ;
	}
	
	public class OnDemandTypeFactory extends TypeFactory {
		
		private static final long serialVersionUID = 1L;
		
		private Set<String> known = new HashSet<String>();
		
		public OnDemandTypeFactory() {
			super(Factory.this);
		}

		@Override
		public <T> CtSimpleType<T> get(String qualifiedName) {
			if (!known.contains(qualifiedName)) {
				known.add(qualifiedName);
				
				int innerTypeIndex = qualifiedName.indexOf(
						CtSimpleType.INNERTTYPE_SEPARATOR);
				if (innerTypeIndex > 0) {
					String containingTypeName = qualifiedName.substring(0, innerTypeIndex);
					get(containingTypeName); // load container (which will also load inner types)
				}
				else {
					CtSimpleType<T> reflection = super.get(qualifiedName);			
		
					if (reflection == null) { 
						// then compilation unit has not yet been built
						
						String pathToBuild = qualifiedName
							.replace('.', File.separatorChar) 
							+ ".java";
						CompilationUnit builtCU = CompilationUnit().find(pathToBuild);				
						if (builtCU == null) {
							// couldn't find compilation unit
							int packIndex = pathToBuild.lastIndexOf(File.separatorChar);
							if (packIndex == -1) {
								// default package. build files in root of source paths
								buildInSourcePaths(".");
							}
							else {
								// build files in containing package
								buildInSourcePaths(pathToBuild.substring(0, packIndex));
							}
						}
					}
				}
			}

			// may still return null, but we've loaded everything we can
			return super.get(qualifiedName);
		}

	}
	
	@Override
	public OnDemandClassFactory Class() {
		return classFactory;
	}
	
	public class OnDemandClassFactory extends ClassFactory {
		private static final long serialVersionUID = 1L;
		private TypeFactory delegateTo;
		
		public OnDemandClassFactory(TypeFactory delegateTo) {
			super(Factory.this);
			this.delegateTo = delegateTo;
		}

		@Override
		public <T> CtClass<T> get(Class<?> cl) {			
			return get(cl.getName());
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> CtClass<T> get(String qualifiedName) {
			try {
				return (CtClass<T>) delegateTo.get(qualifiedName);
			}
			catch (ClassCastException e) {
				return null;
			}
		}
	}

	@Override
	public InterfaceFactory Interface() {
		// TODO: make on-demand if needed
		return super.Interface();
	}
	
	@Override
	public EnumFactory Enum() {
		// TODO: make on-demand if needed
		return super.Enum();
	}
	
	@Override
	public AnnotationFactory Annotation() {
		// TODO: make on-demand if needed
		return super.Annotation();
	}
	
}
