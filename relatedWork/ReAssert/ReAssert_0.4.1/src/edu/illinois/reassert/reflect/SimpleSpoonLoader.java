package edu.illinois.reassert.reflect;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import spoon.reflect.Factory;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.declaration.CtSimpleType;
import spoon.support.ByteCodeOutputProcessor;
import spoon.support.JavaOutputProcessor;

/**
 * Class that implements the minimum environment needed to load, mirror (i.e. 
 * create reflective instances of), modify, and (re)compile classes at runtime 
 * using <a href="http://spoon.gforge.inria.fr/">Spoon</a>. 
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class SimpleSpoonLoader {
	
	public static final String DEFAULT_MODEL_BIN_DIR = ".reassert/spoon/bin";
	public static final String DEFAULT_MODEL_SRC_DIR = ".reassert/spoon/src";
	
	private Factory factory;
	private String modelBinDir = DEFAULT_MODEL_BIN_DIR;
	private String modelSrcDir = DEFAULT_MODEL_SRC_DIR;
	
	public SimpleSpoonLoader(Factory factory) {
		this.factory = factory;
		// make sure we don't clobber the class loader for multiple test runs
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
	}
		
	/**
	 * @return the {@link Factory} used to create mirrors
	 */
	public Factory getFactory() {
		return factory;
	}
	
	/**
	 * Set the temporary directory in which this class outputs source files
	 */
	public void setModelSrcDir(String modelSrcDir) {
		this.modelSrcDir = modelSrcDir;
	}

	/**
	 * Get the temporary directory in which this class outputs source files
	 */
	public String getModelSrcDir() {
		return modelSrcDir;
	}
	
	/**
	 * Set the temporary directory in which this class outputs compiled class files
	 */
	public void setModelBinDir(String modelBinDir) {
		this.modelBinDir = modelBinDir;
	}
	
	/**
	 * Get the temporary directory in which this class outputs compiled class files
	 */
	public String getModelBinDir() {
		return modelBinDir;
	}
	
	public void clean() {
		try {
			File binDir = new File(modelBinDir);
			if (binDir.exists()) {
				FileUtils.deleteDirectory(binDir);
			}
			File srcDir = new File(modelSrcDir);
			if (srcDir.exists()) {
				FileUtils.deleteDirectory(srcDir);
			}
		}
		catch (IOException e) {
			//throw new RuntimeException(e);
		}
	}
	
	/**
	 * Create class files of the internal model in {@link #modelBinDir}
	 */
	public void compile() {
		clean();
		JavaOutputProcessor fileOutput = 
			new JavaOutputProcessor(new File(modelSrcDir));
		ByteCodeOutputProcessor classOutput = 
			new ByteCodeOutputProcessor(fileOutput, new File(modelBinDir));
		classOutput.setFactory(getFactory());
		classOutput.init();
		for (CtSimpleType<?> type : getFactory().Class().getAll()) {
			classOutput.process(type);
		}
		classOutput.processingDone();
	}
	
	/**
	 * Load the class with the given qualified name from the model's build directory 
	 */
	public <T> Class<T> load(String qualifiedName) throws ClassNotFoundException {
		ClassLoader parent = Thread.currentThread().getContextClassLoader();
		return load(qualifiedName, parent);
	}

	/**
	 * Load the class with the given qualified name from the model's build directory,
	 * delegating to the given class loader if necessary.
	 */
	@SuppressWarnings("unchecked")
	public <T> Class<T> load(String qualifiedName, ClassLoader parent)
			throws ClassNotFoundException {
		getFactory().Class().get(qualifiedName); // add class to model
		compile(); // ensure that build dir is up to date
		ClassLoader cl = new BinDirClassLoader(modelBinDir, parent);
		Thread.currentThread().setContextClassLoader(cl); // required for Spoon's getActualClass calls
		return (Class<T>) cl.loadClass(qualifiedName);
	}
	
	public static class BinDirClassLoader extends ClassLoader {
		
		/**
		 * Packages that should be loaded by the system loader.
		 */
		private static final String[] IGNORED_PACKAGES = new String[] {
			"java.", 
			"javax.", 
			"sun.",
			"org.eclipse.",
			"org.xml.",
			"org.w3c.",
			"org.apache.commons.logging.", // class loading breaks logger loading
			"org.junit.", // so JUnit reflection works
			"junit.framework.", // so JUnit reflection works
			"edu.illinois.reassert.", 
		};
		
		/**
		 * Packages that should be loaded by this loader.
		 * Usually subpackages of those ignored by {@link #IGNORED_PACKAGES}.
		 */
		private static final String[] INCLUDED_PACKAGES = new String[] {
			"edu.illinois.reassert.test.",
		};
		
		private Map<String, Class<?>> knownClasses = new HashMap<String, Class<?>>();
		private String modelBinDir;
		
		public BinDirClassLoader(String modelBinDir, ClassLoader parent) {
			super(unwrapParent(parent));
			this.modelBinDir = modelBinDir;
		}

		/**
		 * Find first parent {@link ClassLoader} that is not a {@link BinDirClassLoader} 
		 * to prevent an ever-deepening chain of {@link BinDirClassLoader}s
		 */
		private static ClassLoader unwrapParent(ClassLoader parent) {
			while (parent instanceof BinDirClassLoader) {
				parent = parent.getParent();
			}
			return parent;
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			return loadClass(name, true);
		}
		
		@Override
		protected synchronized Class<?> loadClass(String name, boolean resolve)
				throws ClassNotFoundException {
			if (knownClasses.containsKey(name)) {
				return knownClasses.get(name);
			}
			Class<?> loaded = null;

			URL resource = getResource(name.replace('.', '/') + ".class");
			if (resource == null || isIgnored(name)) {
				loaded = super.loadClass(name, resolve);
			}
			else {
				try {
					InputStream classStream = resource.openStream();
					byte[] classBytes = readToBytes(classStream);
					loaded = defineClass(name, classBytes, 0, classBytes.length);
				}
				catch (IOException e) {
					loaded = super.loadClass(name, resolve);
				}
			}
			
			knownClasses.put(name, loaded);
			return loaded;
		}
		
		private boolean isIgnored(String name) {
			for (String ignored : INCLUDED_PACKAGES) {
				if (name.startsWith(ignored)) {
					return false;
				}
			}
			for (String ignored : IGNORED_PACKAGES) {
				if (name.startsWith(ignored)) {
					return true;
				}
			}
			return false;
		}

		private byte[] readToBytes(InputStream stream) throws IOException {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			int read;
			while ((read = stream.read()) != -1) {
				bytes.write(read);
			}			
			return bytes.toByteArray();
		}

		@Override
		public URL getResource(String name) {
			try {
				File resourceFile = new File(modelBinDir, name);
				if (!resourceFile.exists()) {
					return super.getResource(name);
				}
				return resourceFile.toURI().toURL();
			} 
			catch (MalformedURLException e) {
				// TODO: check for bad bin dir
				throw new RuntimeException(e);
			}
		}

	}

	/**
	 * Outputs the given compilation unit to the given file
	 * @throws IOException if the file cannot be created
	 */
	public void output(CompilationUnit cu, File outFile) throws IOException {
		File modelSrcRoot = new File(modelSrcDir);
		CtSimpleType<?> mainType = cu.getMainType();

		// make sure the latest version of the CU is in the model source dir
		JavaOutputProcessor proc = new JavaOutputProcessor(modelSrcRoot);
		proc.setFactory(getFactory());
		proc.init();
		proc.process(mainType);
		proc.processingDone();
		
		// move the CU file from the model dir to fixFile
		String spoonFileName = mainType.getQualifiedName().replace('.', File.separatorChar) + ".java";
		File spoonFile = new File(modelSrcRoot, spoonFileName);
		FileUtils.copyFile(spoonFile, outFile);
	}

}
