package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.pcm.headless.shared.data.KeyValuePair;
import org.pcm.headless.shared.data.results.DoubleMeasureValue;
import org.pcm.headless.shared.data.results.InMemoryResultRepository;
import org.pcm.headless.shared.data.results.PlainDataSeries;
import org.pcm.headless.shared.data.results.PlainMetricMeasuringPointBundle;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.data.ServiceResult;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.data.ServiceResults;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.data.StatResults;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.util.PalladioAutomationUtil;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.DataBlackboard;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.validation.LongDistribution;

public class PrepareResultsUtil {
	private static final long NANO_TO_MS = 1000L * 1000L;
	private static final String METRIC_ID_RESPONSE_TIME = "_mZb3MdoLEeO-WvSDaR6unQ";

	public static List<StatResults> prepareStatsResults(InMemoryResultRepository b, DataBlackboard blackboard) {
		List<StatResults> inner = new ArrayList<>();

		for (KeyValuePair<PlainMetricMeasuringPointBundle, List<PlainDataSeries>> entry : b.getValues()) {
			try {
				Pair<ServiceEffectSpecification, AssemblyContext> metadata = PalladioAutomationUtil
						.getSeffByMeasuringPoint(blackboard.getLoadedPcm(), entry.getKey().getPoint(),
								entry.getKey().getDesc());

				if (metadata != null && entry.getKey().getDesc().getId().equals(METRIC_ID_RESPONSE_TIME)) {
					StatResults stat = new StatResults();
					ResourceDemandingSEFF demandingSeff = (ResourceDemandingSEFF) metadata.getLeft();
					// create distribution
					LongDistribution analysisDistribution = new LongDistribution();
					LongDistribution monitoringDistribution = new LongDistribution();

					// add all values
					for (int i = 0; i < entry.getValue().size(); i++) {
						if ((i + 1) % 2 == 0) {
							entry.getValue().get(i).getMeasures().forEach(meas -> {
								analysisDistribution.addValue(Math.round(((DoubleMeasureValue) meas.getV()).getV()));
							});
						}
					}

					if (blackboard.getMonitoringData().getServiceCalls().getServiceIds()
							.contains(demandingSeff.getId())) {
						blackboard.getMonitoringData().getServiceCalls().getServiceCalls(demandingSeff.getId())
								.forEach(call -> {
									monitoringDistribution.addValue(call.getResponseTime() / NANO_TO_MS);
								});

						if (monitoringDistribution.size() > 0 && analysisDistribution.size() > 0) {
							stat.setAverageTest(monitoringDistribution.avgTest(analysisDistribution));
							stat.setKsTest(monitoringDistribution.ksTest(analysisDistribution));
							stat.setAvgAnalysis(analysisDistribution.avg());
							stat.setAvgMonitoring(monitoringDistribution.avg());
							stat.setSeffId(demandingSeff.getId());
							stat.setComponentName(
									demandingSeff.getBasicComponent_ServiceEffectSpecification().getEntityName());
							inner.add(stat);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return inner;
	}

	public static ServiceResults prepareServiceResults(InMemoryResultRepository b, DataBlackboard blackboard) {
		ServiceResults ret = new ServiceResults();

		b.getValues().forEach(entry -> {
			Pair<ServiceEffectSpecification, AssemblyContext> metadata = PalladioAutomationUtil.getSeffByMeasuringPoint(
					blackboard.getLoadedPcm(), entry.getKey().getPoint(), entry.getKey().getDesc());

			if (metadata != null) {
				ResourceDemandingSEFF demandingSeff = (ResourceDemandingSEFF) metadata.getLeft();
				// create distribution
				LongDistribution analysisDistribution = new LongDistribution();
				LongDistribution monitoringDistribution = new LongDistribution();

				// add all values
				for (int i = 0; i < entry.getValue().size(); i++) {
					if ((i + 1) % 2 == 0) {
						entry.getValue().get(i).getMeasures().forEach(meas -> {
							analysisDistribution.addValue(Math.round(((DoubleMeasureValue) meas.getV()).getV()));
						});
					}
				}

				if (blackboard.getMonitoringData().getServiceCalls().getServiceIds().contains(demandingSeff.getId())) {
					blackboard.getMonitoringData().getServiceCalls().getServiceCalls(demandingSeff.getId())
							.forEach(call -> {
								monitoringDistribution.addValue(call.getResponseTime() / NANO_TO_MS);
							});

					if (monitoringDistribution.size() > 0 && analysisDistribution.size() > 0) {
						ret.getResults()
								.add(new ServiceResult(
										demandingSeff.getBasicComponent_ServiceEffectSpecification().getEntityName(),
										demandingSeff.getId(), monitoringDistribution, analysisDistribution));
					}

				}
			}
		});

		return ret;
	}

}
