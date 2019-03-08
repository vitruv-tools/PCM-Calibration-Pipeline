package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.data.ServiceResult;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.data.ServiceResults;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.data.StatResults;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.results.PalladioAnalysisResults;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.util.PalladioAutomationUtil;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.DataBlackboard;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.validation.LongDistribution;

public class PrepareResultsUtil {
	private static final long NANO_TO_MS = 1000L * 1000L;

	public static List<StatResults> prepareStatsResults(PalladioAnalysisResults b, DataBlackboard blackboard) {
		List<StatResults> inner = new ArrayList<>();

		b.entries().forEach(entry -> {
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
				if (entry.getValue().getYValues().size() > 0 && blackboard.getMonitoringData().getServiceCalls()
						.getServiceIds().contains(demandingSeff.getId())) {
					blackboard.getMonitoringData().getServiceCalls().getServiceCalls(demandingSeff.getId())
							.forEach(call -> {
								monitoringDistribution.addValue(call.getResponseTime() / NANO_TO_MS);
							});

					stat.setAverageTest(monitoringDistribution.avgTest(analysisDistribution));
					stat.setKsTest(monitoringDistribution.ksTest(analysisDistribution));
					stat.setAvgAnalysis(analysisDistribution.avg());
					stat.setAvgMonitoring(monitoringDistribution.avg());
					stat.setSeffId(demandingSeff.getId());
					stat.setComponentName(demandingSeff.getBasicComponent_ServiceEffectSpecification().getEntityName());
					inner.add(stat);
				}
			}
		});

		return inner;
	}

	public static ServiceResults prepareServiceResults(PalladioAnalysisResults b, DataBlackboard blackboard) {
		ServiceResults ret = new ServiceResults();

		b.entries().forEach(entry -> {
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
				if (entry.getValue().getYValues().size() > 0 && blackboard.getMonitoringData().getServiceCalls()
						.getServiceIds().contains(demandingSeff.getId())) {
					blackboard.getMonitoringData().getServiceCalls().getServiceCalls(demandingSeff.getId())
							.forEach(call -> {
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
