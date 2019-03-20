package tools.vitruv.applications.pcmjava.modelrefinement.parameters.load;

import java.io.File;
import java.io.IOException;

public class JMeterExecutor {
	private final String JMETER_PATH;

	public JMeterExecutor(String jMeterPath) {
		JMETER_PATH = jMeterPath;
	}

	public boolean execute(File file) throws IOException {
		if (file != null) {
			ProcessBuilder builder = new ProcessBuilder(JMETER_PATH, "-n", "-t", "\"" + file.getAbsolutePath() + "\"");
			try {
				builder.start().waitFor();
				return true;
			} catch (InterruptedException e) {
				return false;
			}
		}
		return false;
	}

}
