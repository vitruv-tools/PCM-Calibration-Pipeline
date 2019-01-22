package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.cluster.SessionCluster;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.cluster.UsageSessionClusterer;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.session.UsageSession;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class UsageModelExtractor {
	private static final String NOT_SET = "<not set>";

	private UsageModel model;
	private System system;
	private Repository repository;

	private Map<String, UsageSession> sessions;
	private UsageSessionClusterer clusterer;

	public UsageModelExtractor(Repository repository, UsageModel initial, System system) {
		this.model = initial;
		this.system = system;
		this.repository = repository;

		this.sessions = new HashMap<>();
		this.clusterer = new UsageSessionClusterer();
	}

	// TODO also inherit timings between the actions in the cluster
	public List<SessionCluster> extractUserGroups(MonitoringDataSet monitoringData, float similaryThres,
			int minInheritCount) {
		// get all system entry calls
		Set<String> entrySignatureIds = PcmUtils.getProvidedOperations(system).stream().map(op -> op.getId())
				.collect(Collectors.toSet());

		// collect service calls
		monitoringData.getServiceCalls().getServiceCalls().forEach(call -> {
			if (!call.getSessionId().equals(NOT_SET) && isSystemEntryCall(entrySignatureIds, call)) {
				if (!sessions.containsKey(call.getSessionId())) {
					sessions.put(call.getSessionId(), new UsageSession(call.getEntryTime()));
				}
				sessions.get(call.getSessionId()).update(call);
			}
		});

		// cluster
		List<UsageSession> rawSession = sessions.entrySet().stream().map(entry -> entry.getValue())
				.collect(Collectors.toList());
		List<SessionCluster> clusters = this.clusterer.clusterSessions(rawSession, similaryThres);

		// build scenarios
		return clusters.stream().filter(cluster -> cluster.getInheritCount() >= minInheritCount)
				.collect(Collectors.toList());
	}

	private boolean isSystemEntryCall(Set<String> sigs, ServiceCall call) {
		ServiceEffectSpecification spec = PcmUtils.getElementById(repository, ResourceDemandingSEFF.class,
				call.getServiceId());
		if (spec != null) {
			return sigs.contains(spec.getDescribedService__SEFF().getId());
		}
		return false;
	}

	public void set(UsageModel current) {
		this.model = current;
	}

	public UsageModel get() {
		return this.model;
	}

}
