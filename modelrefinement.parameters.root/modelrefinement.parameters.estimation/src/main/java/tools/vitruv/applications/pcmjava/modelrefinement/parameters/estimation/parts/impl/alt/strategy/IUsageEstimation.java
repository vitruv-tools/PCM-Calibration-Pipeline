package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.strategy;

import java.util.List;
import java.util.Map;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.timeline.AbstractTimelineObject;

public interface IUsageEstimation {

	public Map<AbstractTimelineObject, Double> splitUpUsage(double usage, long startInterval,
			long stopInterval, List<AbstractTimelineObject> objects);

}
