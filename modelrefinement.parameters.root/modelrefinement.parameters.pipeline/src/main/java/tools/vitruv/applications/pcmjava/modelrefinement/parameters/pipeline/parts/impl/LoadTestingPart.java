package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl;

import java.io.File;
import java.io.IOException;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.load.JMeterExecutor;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.PipelineState;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;

public class LoadTestingPart extends AbstractPipelinePart {
	private String jmxPath;
	private JMeterExecutor executor;

	public LoadTestingPart(String jMeterPath, String jmxPath) {
		super(false);

		this.jmxPath = jmxPath;
		this.executor = new JMeterExecutor(jMeterPath);
	}

	@Override
	protected void execute() {
		logger.info("Started load testing.");
		getBlackboard().setState(PipelineState.LOAD_TESTING);

		try {
			this.executor.execute(new File(jmxPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
