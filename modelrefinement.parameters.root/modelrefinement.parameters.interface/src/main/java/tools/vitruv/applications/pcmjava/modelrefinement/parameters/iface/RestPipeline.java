package tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.data.ServiceResult;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.data.ServiceResults;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.data.StatResults;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.util.PalladioAutomationUtil;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.IPipelineStateListener;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.ParameterEstimationPipeline;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.PipelineState;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.config.EPAPipelineConfiguration;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.validation.LongDistribution;

public class RestPipeline extends ParameterEstimationPipeline implements IPipelineStateListener {
	private static final long NANO_TO_MS = 1000L * 1000L;

	private RestInterface rest;

	public RestPipeline(RestInterface rest, EPAPipelineConfiguration pipelineConfiguration) {
		super(pipelineConfiguration);
		this.rest = rest;
		this.rest.setPipeline(this);

		this.blackboard.getListeners().add(this);

		// set init state
		blackboard.setState(PipelineState.INIT);
	}

	@Override
	public void onChange(PipelineState nState) {
		this.rest.setCurrentState(nState);
	}

	// TODO this code duplication is a bit ugly
	protected List<StatResults> provideStatsJson() {
		final List<StatResults> ret = new ArrayList<>();
		if (blackboard.getAnalysisResults() == null || blackboard.getAnalysisResults().size() == 0) {
			return Collections.emptyList();
		}
		final MonitoringDataSet monitoringData = blackboard.getMonitoringData();

		int lastId = blackboard.getAnalysisResults().size() - 1;

		blackboard.getAnalysisResults().get(lastId).entries().forEach(entry -> {
			Pair<ServiceEffectSpecification, AssemblyContext> metadata = PalladioAutomationUtil.getSeffByMeasuringPoint(
					blackboard.getLoadedPcm().getRepository(), blackboard.getLoadedPcm().getUsageModel(),
					blackboard.getLoadedPcm().getSystem(), entry.getKey(), entry.getValue().getMetricDescription());

			if (metadata != null) {
				StatResults stat = new StatResults();
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

					stat.setAverageTest(monitoringDistribution.avgTest(analysisDistribution));
					stat.setKsTest(monitoringDistribution.ksTest(analysisDistribution));
					stat.setAvgAnalysis(analysisDistribution.avg());
					stat.setAvgMonitoring(monitoringDistribution.avg());
					stat.setSeffId(demandingSeff.getId());
					stat.setComponentName(demandingSeff.getBasicComponent_ServiceEffectSpecification().getEntityName());
					ret.add(stat);
				}
			}
		});

		return ret;
	}

	protected ServiceResults provideResultsJson() {
		final ServiceResults ret = new ServiceResults();
		if (blackboard.getAnalysisResults() == null || blackboard.getAnalysisResults().size() == 0) {
			return ret;
		}
		final MonitoringDataSet monitoringData = blackboard.getMonitoringData();

		int lastId = blackboard.getAnalysisResults().size() - 1;

		blackboard.getAnalysisResults().get(lastId).entries().forEach(entry -> {
			Pair<ServiceEffectSpecification, AssemblyContext> metadata = PalladioAutomationUtil.getSeffByMeasuringPoint(
					blackboard.getLoadedPcm().getRepository(), blackboard.getLoadedPcm().getUsageModel(),
					blackboard.getLoadedPcm().getSystem(), entry.getKey(), entry.getValue().getMetricDescription());

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

					ret.getResults()
							.add(new ServiceResult(
									demandingSeff.getBasicComponent_ServiceEffectSpecification().getEntityName(),
									demandingSeff.getId(), monitoringDistribution, analysisDistribution));
				}
			}
		});

		return ret;
	}

}
