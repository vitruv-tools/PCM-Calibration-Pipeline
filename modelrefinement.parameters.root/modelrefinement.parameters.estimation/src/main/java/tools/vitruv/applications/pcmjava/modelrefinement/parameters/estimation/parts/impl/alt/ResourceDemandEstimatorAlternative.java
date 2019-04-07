package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.seff_performance.ParametricResourceDemand;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.IResourceDemandEstimator;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.timeline.IResourceDemandTimeline;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.timeline.ResourceDemandTimeline;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.timeline.ResourceDemandTimelineInterval;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.timeline.TimelineAnalyzer;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.InMemoryPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class ResourceDemandEstimatorAlternative implements IResourceDemandEstimator {

	private InMemoryPCM pcm;

	private List<IResourceDemandTimeline> timelines;

	public ResourceDemandEstimatorAlternative(InMemoryPCM pcm) {
		this.pcm = pcm;
		this.timelines = new ArrayList<>();
	}

	@Override
	// TODO performance is not that good, POC only atm
	public void prepare(MonitoringDataSet data) {
		// build service call tree
		List<TreeNode<ServiceCall>> callRoots = new ArrayList<>();
		Map<String, TreeNode<ServiceCall>> idMapping = new HashMap<>();

		// sort
		List<ServiceCall> sortedServiceCalls = data.getServiceCalls().getServiceCalls().stream().sorted((a, b) -> {
			if (a.getEntryTime() < b.getEntryTime()) {
				return -1;
			} else if (a.getEntryTime() > b.getEntryTime()) {
				return 1;
			} else {
				return 0;
			}
		}).collect(Collectors.toList());

		// get all service calls
		for (ServiceCall call : sortedServiceCalls) {
			if (call.getCallerServiceExecutionId().equals("<not set>")) {
				// insert root
				TreeNode<ServiceCall> root = new TreeNode<>(call);
				callRoots.add(root);
				idMapping.put(call.getServiceExecutionId(), root);
			} else {
				// get parent
				TreeNode<ServiceCall> parent = idMapping.get(call.getCallerServiceExecutionId());
				TreeNode<ServiceCall> nNode = parent.addChild(call);
				idMapping.put(call.getServiceExecutionId(), nNode);
			}
		}

		// timeline mapping
		Map<Pair<String, String>, IResourceDemandTimeline> timelineMapping = new HashMap<>();

		// process all roots
		for (TreeNode<ServiceCall> root : callRoots) {
			String assembly = root.data.getAssemblyId();
			AssemblyContext ctx = PcmUtils.getElementById(pcm.getSystem(), AssemblyContext.class, assembly);
			ResourceDemandingSEFF seff = PcmUtils.getElementById(pcm.getRepository(), ResourceDemandingSEFF.class,
					root.data.getServiceId());

			ResourceContainer container = getContainerByAssemblyId(ctx);

			Set<String> resourceIds = getDemandingResources(seff);
			for (String resourceId : resourceIds) {
				Pair<String, String> tempPair = Pair.of(container.getId(), resourceId);
				if (!timelineMapping.containsKey(tempPair)) {
					timelineMapping.put(tempPair, new ResourceDemandTimeline(container.getId(), resourceId));
				}
				timelineMapping.get(tempPair).addInterval(new ResourceDemandTimelineInterval(root, data, resourceId),
						root.data.getEntryTime());
			}

		}

		// process all cpu rates
		timelineMapping.entrySet().forEach(entry -> {
			String containerId = entry.getKey().getLeft();
			String resourceId = entry.getKey().getRight();

			IResourceDemandTimeline timeline = entry.getValue();

			Map<String, SortedMap<Long, Double>> resourceUtils = data.getResourceUtilizations()
					.getContainerUtilization(containerId);
			if (resourceUtils != null) {
				resourceUtils.entrySet().stream().filter(util -> util.getKey().equals(resourceId)).forEach(util -> {
					util.getValue().entrySet().forEach(et -> timeline.addUtilization(et.getKey(), et.getValue()));
				});
			}
		});

		// timelines
		this.timelines = timelineMapping.entrySet().stream().map(entry -> entry.getValue())
				.collect(Collectors.toList());

	}

	@Override
	public void derive() {
		TimelineAnalyzer analyzer = new TimelineAnalyzer(pcm, 10000); // 10 s
		for (IResourceDemandTimeline tl : this.timelines) {
			analyzer.analyze(tl);
		}
	}

	private Set<String> getDemandingResources(ResourceDemandingSEFF seff) {
		return PcmUtils.getObjects(seff, ParametricResourceDemand.class).stream()
				.filter(demand -> demand.getRequiredResource_ParametricResourceDemand() != null)
				.map(demand -> demand.getRequiredResource_ParametricResourceDemand().getId())
				.collect(Collectors.toSet());
	}

	private ResourceContainer getContainerByAssemblyId(AssemblyContext asCtx) {
		return PcmUtils.getObjects(pcm.getAllocationModel(), AllocationContext.class).stream().filter(ctx -> {
			return ctx.getAssemblyContext_AllocationContext().getId().equals(asCtx.getId());
		}).map(t -> t.getResourceContainer_AllocationContext()).findFirst().orElse(null);
	}

}
