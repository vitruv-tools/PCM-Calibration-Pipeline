package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.pcm.headless.shared.data.results.InMemoryResultRepository;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.util.PalladioAutomationUtil;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.PipelineState;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.InMemoryPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.validation.LongDistribution;

public class CrossValidationPart extends AbstractPipelinePart {
	private static final long NANO_TO_MS = 1000L * 1000L;

	public CrossValidationPart() {
		super(false);
	}

	@Override
	protected void execute() {
		logger.info("Comparing the Simucom Analysis results with the monitoring data.");

		// state
		getBlackboard().setState(PipelineState.EVALUATION);

		InMemoryResultRepository results = getBlackboard().getAnalysisResults()
				.get(getBlackboard().getAnalysisResults().size() - 1);

		final InMemoryPCM pcm = getBlackboard().getLoadedPcm();

		if (results == null) {
			logger.warn("Simulation results are not valid or a timeout occurred.");
			return;
		}

		MonitoringDataSet monitoringData = getBlackboard().getMonitoringData();

		results.getValues().forEach(entry -> {
			Pair<ServiceEffectSpecification, AssemblyContext> metadata = PalladioAutomationUtil
					.getSeffByMeasuringPoint(pcm, entry.getKey().getPoint(), entry.getKey().getDesc());

			if (metadata != null) {
				ResourceDemandingSEFF demandingSeff = (ResourceDemandingSEFF) metadata.getLeft();
				// create distribution
				LongDistribution analysisDistribution = new LongDistribution();
				LongDistribution monitoringDistribution = new LongDistribution();

				// add all values

				if (analysisDistribution.size() > 0
						&& monitoringData.getServiceCalls().getServiceIds().contains(demandingSeff.getId())) {
					monitoringData.getServiceCalls().getServiceCalls(demandingSeff.getId()).forEach(call -> {
						monitoringDistribution.addValue(call.getResponseTime() / NANO_TO_MS);
					});

					compareDistributions(demandingSeff, analysisDistribution, monitoringDistribution);
				}
			}
		});

		// set finished
		getBlackboard().setState(PipelineState.FINISHED);
	}

	private void compareDistributions(ResourceDemandingSEFF seff, LongDistribution analysisDistribution,
			LongDistribution monitoringDistribution) {
		double ks_ab = analysisDistribution.ksTest(monitoringDistribution);

		logger.info("------------- Service: " + seff.getId() + " -------------");
		logger.info("KS TEST = " + ks_ab);
		logger.info("Average analysis: " + analysisDistribution.avg() + "ms");
		logger.info("Average monitoring: " + monitoringDistribution.avg() + "ms");
		logger.info("Average TEST = " + (analysisDistribution.avgTest(monitoringDistribution) * 100) + "%");
	}

}
