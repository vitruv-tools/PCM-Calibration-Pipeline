package tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.results.MeasuringPointResults;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.results.PalladioAnalysisResults;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.util.MetricType;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.util.PalladioAutomationUtil;

public class ServiceAnalysisResults {

	private Map<ServiceEffectSpecification, Map<AssemblyContext, List<MeasuringPointResults>>> serviceResults;

	public static ServiceAnalysisResults buildFromAnalysisResults(Repository repo, System system, UsageModel usage,
			PalladioAnalysisResults results) {
		ServiceAnalysisResults build = new ServiceAnalysisResults();

		results.entries().forEach(res -> {
			if (PalladioAutomationUtil
					.getMetricType(res.getValue().getMetricDescription()) == MetricType.RESPONSE_TIME) {
				Pair<ServiceEffectSpecification, AssemblyContext> mappedSeff = PalladioAutomationUtil
						.getSeffByMeasuringPoint(repo, usage, system, res.getKey(),
								res.getValue().getMetricDescription());

				if (!build.serviceResults.containsKey(mappedSeff.getLeft())) {
					build.serviceResults.put(mappedSeff.getLeft(), new HashMap<>());
				}

				Map<AssemblyContext, List<MeasuringPointResults>> innerResults = build.serviceResults
						.get(mappedSeff.getLeft());
				if (!innerResults.containsKey(mappedSeff.getRight())) {
					innerResults.put(mappedSeff.getRight(), new ArrayList<>());
				}

				innerResults.get(mappedSeff.getRight()).add(res.getValue());
			}
		});

		return build;
	}

	private ServiceAnalysisResults() {
		this.serviceResults = new HashMap<>();
	}

	public Set<ServiceEffectSpecification> getTrackedSEFFs() {
		return serviceResults.keySet();
	}

	public List<MeasuringPointResults> getResultsBySEFF(ServiceEffectSpecification spec) {
		if (!serviceResults.containsKey(spec))
			return null;

		return serviceResults.get(spec).entrySet().stream().map(entry -> entry.getValue())
				.flatMap(list -> list.stream()).collect(Collectors.toList());
	}

}
