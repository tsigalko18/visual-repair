package edu.illinois.reassert.plugin;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ReAssertPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.illinois.reassert.plugin.ReAssertPlugin";

	public static final String FIX_STRATEGY_EXTENSION_POINT = "edu.illinois.reassert.plugin.fixstrategy";
	public static final String CONSOLE_NAME = "ReAssert Console";

	// The shared instance
	private static ReAssertPlugin plugin;

	
	/**
	 * The constructor
	 */
	public ReAssertPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ReAssertPlugin getDefault() {
		return plugin;
	}

}
