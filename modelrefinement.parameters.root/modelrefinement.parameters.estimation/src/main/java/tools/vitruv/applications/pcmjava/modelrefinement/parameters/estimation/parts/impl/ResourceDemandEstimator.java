package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

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

	private Map<ParametricResourceDemand, IResourceDemandModel> resourceDemandMapping;
	private InMemoryPCM pcm;
	private MonitoringDataSet monitoringData;

	public ResourceDemandEstimator(InMemoryPCM pcm) {
		this.resourceDemandMapping = new HashMap<>();
		this.pcm = pcm;
	}

	@Override
	public void prepare(MonitoringDataSet data) {
		this.monitoringData = data;

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
							String resId = recs.get(0).getResourceId();
							for (ResponseTimeRecord record : recs) {
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
		for (Entry<ParametricResourceDemand, IResourceDemandModel> entry : this.resourceDemandMapping.entrySet()) {
			PCMRandomVariable newDemand = entry.getValue().deriveStochasticExpression(0.7f);
			if (newDemand != null) {
				entry.getKey().setSpecification_ParametericResourceDemand(newDemand);
			}
		}
	}

	private void buildResourceDemandTriple(ParametricResourceDemand demand,
			ProcessingResourceSpecification containerResource, long duration, ServiceParameters parameters) {
		ResourceDemandTriple nTriple = new ResourceDemandTriple(demand.getAction_ParametricResourceDemand().getId(),
				containerResource, demand, duration);
		nTriple.setParameters(parameters);

		if (!this.resourceDemandMapping.containsKey(demand)) {
			this.resourceDemandMapping.put(demand, new ResourceDemandModel());
		}
		this.resourceDemandMapping.get(demand).put(nTriple);
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
