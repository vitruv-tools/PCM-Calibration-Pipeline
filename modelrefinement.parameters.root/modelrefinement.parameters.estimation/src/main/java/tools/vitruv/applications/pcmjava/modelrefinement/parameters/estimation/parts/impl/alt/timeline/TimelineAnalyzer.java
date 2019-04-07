package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.timeline;

import java.util.List;
import java.util.Map.Entry;

import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.TreeNode;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.InMemoryPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class TimelineAnalyzer implements ITimelineAnalysis {

	private long maxDurationMs;
	private InMemoryPCM pcm;

	public TimelineAnalyzer(InMemoryPCM pcm, long maxDurationMs) {
		this.maxDurationMs = maxDurationMs;
		this.pcm = pcm;
	}

	@Override
	public void analyze(IResourceDemandTimeline timeline) {
		// calculate baseline (system, ...)
		double utilBaseline = calculateBaseline(timeline, maxDurationMs * 1000000L);

		// iterate over cpu intervals
		Entry<Long, Double> before = null;
		for (Entry<Long, Double> entry : timeline.getAllUtilizations()) {
			if (before != null) {
				List<Entry<Long, ResourceDemandTimelineInterval>> intersecting = timeline
						.getIntersectingIntervals(before.getKey(), entry.getKey(), maxDurationMs * 1000000L);

				if (intersecting.size() > 0) {
					// normalized util
					double utilizationNormalized = ((before.getValue() + entry.getValue()) / 2d) - utilBaseline;

					// scale it up to 1
					utilizationNormalized *= 1d / (1d - utilBaseline);

					if (utilizationNormalized > 0.0) {
						for (Entry<Long, ResourceDemandTimelineInterval> intersection : intersecting) {
							unrollIntervalWithModel(intersection.getValue());
						}
					}
				}
			}

			before = entry;
		}
	}

	private void unrollIntervalWithModel(ResourceDemandTimelineInterval ival) {
		unrollIntervalWithModel(ival.getRoot());
	}

	private void unrollIntervalWithModel(TreeNode<AbstractResourceDemandTimelineObject> obj) {
		if (obj.data instanceof ServiceCallTimelineObject) {
			long iAs = obj.children.stream().filter(f -> f.data instanceof InternalActionTimelineObject)
					.map(k -> ((InternalActionTimelineObject) k.data).getInternalActionId()).distinct().count();
			ServiceCallTimelineObject tlo = (ServiceCallTimelineObject) obj.data;
			ResourceDemandingSEFF seff = PcmUtils.getElementById(pcm.getRepository(), ResourceDemandingSEFF.class,
					tlo.getServiceId());

			if (seff != null) {
				System.out.println(iAs);
				int iAsPCM = PcmUtils.getObjects(seff, InternalAction.class).size();
				System.out.println("->" + iAsPCM);
			}
		}
	}

	private double calculateBaseline(IResourceDemandTimeline timeline, long maxDuration) {
		double sum = 0;
		int num = 0;

		Entry<Long, Double> before = null;
		for (Entry<Long, Double> entry : timeline.getAllUtilizations()) {
			if (before != null) {
				if (timeline.getIntersectingIntervals(before.getKey(), entry.getKey(), maxDuration).size() == 0) {
					if (!Double.isNaN(before.getValue()) && !Double.isNaN(entry.getValue())) {
						num++;
						sum += (before.getValue() + entry.getValue()) / 2d;
					}
				}
			}

			before = entry;
		}

		return num == 0 ? 0 : sum / num;
	}

}
