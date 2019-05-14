package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.PipelineState;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;

public class LoadMonitoringDataPart extends AbstractPipelinePart {

	private String monitoringDataPath;

	public LoadMonitoringDataPart(String monitoringDataPath) {
		super(false); // always executed
		this.monitoringDataPath = monitoringDataPath;
	}

	@Override
	protected void execute() {
		long loadStart = System.currentTimeMillis();
		logger.info("Loading monitoring data.");

		getBlackboard().setState(PipelineState.LOAD_MONITORING_DATA);

		this.getBlackboard().setMonitoringData(new KiekerMonitoringReader(monitoringDataPath));
		logger.info("Loading all monitoring data needed " + (System.currentTimeMillis() - loadStart) + "ms.");
	}

}
