package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl;

import org.pcm.headless.shared.data.ESimulationType;
import org.pcm.headless.shared.data.config.HeadlessSimulationConfig;
import org.pcm.headless.shared.data.results.InMemoryResultRepository;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.PalladioHeadlessExecutor;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.PipelineState;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.InMemoryPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;

public class PalladioExecutorPart extends AbstractPipelinePart {
	private PalladioHeadlessExecutor executor;

	private volatile boolean finished;

	public PalladioExecutorPart(String url) {
		super(false);

		executor = new PalladioHeadlessExecutor(url);
	}

	@Override
	protected void execute() {
		logger.info("Starting PCM Simucom Analysis.");
		finished = false;

		getBlackboard().setState(PipelineState.PCM_ANALYSIS);

		// build experiments repo
		getBlackboard().persistInMemoryPCM();
		InMemoryPCM currentPCM = getBlackboard().getLoadedPcm();

		// TODO also v∂ariables not hardcoded maybe
		HeadlessSimulationConfig simConfig = HeadlessSimulationConfig.builder()
				.experimentName("Automatic Palladio Execution").repetitions(1).maximumMeasurementCount(50000)
				.simulationTime(1000000).type(ESimulationType.SIMUCOM).build();

		// perform the analysis
		// TODO blocking approach is not the best way here
		try {
			executor.triggerSimulation(currentPCM, simConfig, res -> {
				processResults(res);
			});
			while (!finished) {
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			logger.warn("Failed to perform the Analysis correctly.");
		}

	}

	private void processResults(InMemoryResultRepository res) {
		logger.info("Simulation finished.");
		finished = true;

		this.getBlackboard().addAnalysisResults(res);
	}

}
