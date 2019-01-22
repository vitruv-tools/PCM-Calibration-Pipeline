package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.results.PalladioAnalysisResults;
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

		PalladioAnalysisResults results = getBlackboard().getAnalysisResults();

		final InMemoryPCM pcm = getBlackboard().getLoadedPcm();
		final Repository repository = pcm.getRepository();
		final UsageModel usagemodel = pcm.getUsageModel();
		final System system = pcm.getSystem();

		MonitoringDataSet monitoringData = getBlackboard().getMonitoringData();

		results.entries().forEach(entry -> {
			Pair<ServiceEffectSpecification, AssemblyContext> metadata = PalladioAutomationUtil.getSeffByMeasuringPoint(
					repository, usagemodel, system, entry.getKey(), entry.getValue().getMetricDescription());

			if (metadata != null) {
				ResourceDemandingSEFF demandingSeff = (ResourceDemandingSEFF) metadata.getLeft();
				// create distribution
				LongDistribution analysisDistribution = new LongDistribution();
				LongDistribution monitoringDistribution = new LongDistribution();

				// add all values
				entry.getValue().getYValues().forEach(y -> analysisDistribution.addValue(y.getValue().longValue()));
				if (entry.getValue().getYValues().size() > 0
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
