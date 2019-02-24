package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.seff_performance.ParametricResourceDemand;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceParameters;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.data.ResourceDemandTriple;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.IResourceDemandEstimator;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.IResourceDemandModel;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.util.EstimationUtil;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.records.ResponseTimeRecord;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.InMemoryPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class ResourceDemandEstimator implements IResourceDemandEstimator {

	private Map<ParametricResourceDemand, Map<ProcessingResourceSpecification, IResourceDemandModel>> resourceDemandMapping;
	private Map<ProcessingResourceSpecification, List<Double>> resourceSpecs;

	private InMemoryPCM pcm;
	private MonitoringDataSet monitoringData;

	public ResourceDemandEstimator(InMemoryPCM pcm) {
		this.resourceDemandMapping = new HashMap<>();
		this.resourceSpecs = new HashMap<>();
		this.pcm = pcm;
	}

	@Override
	public void prepare(MonitoringDataSet data) {
		this.monitoringData = data;
		this.resourceDemandMapping.clear();
		this.resourceSpecs.clear();

		for (String serviceId : data.getServiceCalls().getServiceIds()) {
			ResourceDemandingSEFF seff = PcmUtils.getElementById(pcm.getRepository(), ResourceDemandingSEFF.class,
					serviceId);
			List<ParametricResourceDemand> innerResourceDemands = PcmUtils.getObjects(seff,
					ParametricResourceDemand.class);

			for (ParametricResourceDemand resourceDemand : innerResourceDemands) {
				for (ServiceCall call : data.getServiceCalls().getServiceCalls(serviceId)) {
					AssemblyContext ctx = PcmUtils.getElementById(pcm.getSystem(), AssemblyContext.class,
							call.getAssemblyId());
					if (ctx == null) {
						ctx = EstimationUtil.getAssemblyBySeff(pcm.getRepository(), pcm.getSystem(), serviceId);
					}

					if (ctx != null) {
						ResourceContainer container = EstimationUtil.getContainerByAssemblyContext(ctx,
								pcm.getAllocationModel());

						List<ResponseTimeRecord> recs = getResponseTimes(resourceDemand, call);

						if (recs != null) {
							for (ResponseTimeRecord record : recs) {
								String resId = record.getResourceId();

								ProcessingResourceSpecification spec = container
										.getActiveResourceSpecifications_ResourceContainer().stream()
										.filter(p -> p.getActiveResourceType_ActiveResourceSpecification().getId()
												.equals(resId))
										.findFirst().orElse(null);

								if (spec != null) {
									buildResourceDemandTriple(resourceDemand,
											container.getActiveResourceSpecifications_ResourceContainer().get(0),
											record.getStopTime() - record.getStartTime(), call.getParameters());
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void derive() {
		// derive resource demands
		for (Entry<ParametricResourceDemand, Map<ProcessingResourceSpecification, IResourceDemandModel>> entry : this.resourceDemandMapping
				.entrySet()) {
			// collect all PCM vars
			List<Triple<ProcessingResourceSpecification, PCMRandomVariable, Double[]>> pairs = new ArrayList<>();

			for (Entry<ProcessingResourceSpecification, IResourceDemandModel> innerEntry : entry.getValue()
					.entrySet()) {
				Pair<PCMRandomVariable, Double[]> inner = innerEntry.getValue().deriveStochasticExpression(0.7f);
				if (inner != null) {
					pairs.add(Triple.of(innerEntry.getKey(), inner.getKey(), inner.getValue()));
				}
			}

			// calculate formula and add belonging resource scale
			int minIndex = -1;
			double minSum = -1;
			for (int k = 0; k < pairs.size(); k++) {
				Triple<ProcessingResourceSpecification, PCMRandomVariable, Double[]> currentEntry = pairs.get(k);
				if (currentEntry != null) {
					double sum = Arrays.stream(currentEntry.getRight()).mapToDouble(Double::doubleValue).sum();
					if (minIndex == -1 || sum < minSum) {
						minSum = sum;
						minIndex = k;
					}
				}
			}

			if (minIndex >= 0) {
				// set lowest as reference
				entry.getKey().setSpecification_ParametericResourceDemand(pairs.get(minIndex).getMiddle());

				// calculate belonging resource powers
				Double[] reference = pairs.get(minIndex).getRight();
				pairs.forEach(pair -> {
					double sum = 0;
					for (int z = 1; z < Math.min(reference.length, pair.getRight().length); z++) {
						sum += reference[z].doubleValue() / pair.getRight()[z].doubleValue();
					}
					sum /= (double) (Math.min(reference.length, pair.getRight().length) - 1d);

					if (!this.resourceSpecs.containsKey(pair.getLeft())) {
						this.resourceSpecs.put(pair.getLeft(), new ArrayList<>());
					}
					this.resourceSpecs.get(pair.getLeft()).add(sum);
				});
			}
		}

		// recalculate processing resources
		for (Entry<ProcessingResourceSpecification, List<Double>> recordedPower : this.resourceSpecs.entrySet()) {
			// TODO use other metric than average here
			if (recordedPower.getValue().size() > 0) {
				PCMRandomVariable var = CoreFactory.eINSTANCE.createPCMRandomVariable();
				var.setSpecification(
						String.valueOf(recordedPower.getValue().stream().mapToDouble(d -> d).average().getAsDouble()));
				recordedPower.getKey().setProcessingRate_ProcessingResourceSpecification(var);
			}
		}
	}

	private void buildResourceDemandTriple(ParametricResourceDemand demand,
			ProcessingResourceSpecification containerResource, long duration, ServiceParameters parameters) {
		ResourceDemandTriple nTriple = new ResourceDemandTriple(demand.getAction_ParametricResourceDemand().getId(),
				containerResource, demand, duration);
		nTriple.setParameters(parameters);

		if (!this.resourceDemandMapping.containsKey(demand)) {
			this.resourceDemandMapping.put(demand, new HashMap<>());
		}
		if (!this.resourceDemandMapping.get(demand).containsKey(containerResource)) {
			this.resourceDemandMapping.get(demand).put(containerResource, new ResourceDemandModel());
		}
		this.resourceDemandMapping.get(demand).get(containerResource).put(nTriple);
	}

	private List<ResponseTimeRecord> getResponseTimes(ParametricResourceDemand demand, ServiceCall parent) {
		if (demand.getAction_ParametricResourceDemand() instanceof InternalAction) {
			String actionId = demand.getAction_ParametricResourceDemand().getId();
			Set<String> resourceIds = this.monitoringData.getResponseTimes().getResourceIds(actionId);
			// TODO support multiple resource ids
			if (resourceIds != null && resourceIds.size() == 1) {
				String resourceId = resourceIds.stream().findFirst().orElse(null);
				return this.monitoringData.getResponseTimes().getResponseTimes(actionId, resourceId).stream()
						.filter(action -> action.getServiceExecutionId().equals(parent.getServiceExecutionId()))
						.collect(Collectors.toList());
			}
		}
		return null;
	}

}
