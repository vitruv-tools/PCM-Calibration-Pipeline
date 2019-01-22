package tools.vitruv.applications.pcmjava.modelrefinement.parameters.load;

import java.io.File;
import java.io.IOException;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

public class JMeterExecutor {
	private final String JMETER_PATH;

	public JMeterExecutor(String jMeterPath) {
		JMETER_PATH = jMeterPath;
	}

	public void execute(File file) throws IOException {
		// JMeter Engine
		StandardJMeterEngine jmeter = new StandardJMeterEngine();

		// Initialize Properties, logging, locale, etc.
		JMeterUtils.loadJMeterProperties(JMETER_PATH + File.separator + "bin" + File.separator + "jmeter.properties");
		JMeterUtils.setJMeterHome(JMETER_PATH);
		JMeterUtils.initLocale();

		// Initialize JMeter SaveService
		SaveService.loadProperties();

		// Load existing .jmx Test Plan
		HashTree testPlanTree = SaveService.loadTree(file);

		System.out.println(testPlanTree.keySet());

		// TODO adjust tree and create a test plan with appropriate properties

		// Run JMeter Test
		jmeter.configure(testPlanTree);
		jmeter.run();
	}

}
