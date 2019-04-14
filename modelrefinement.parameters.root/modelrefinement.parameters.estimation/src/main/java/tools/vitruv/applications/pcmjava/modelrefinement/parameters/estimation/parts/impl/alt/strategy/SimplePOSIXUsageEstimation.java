package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.timeline.AbstractTimelineObject;

public class SimplePOSIXUsageEstimation implements IUsageEstimation {

	@Override
	public Map<AbstractTimelineObject, Double> splitUpUsage(double usage, long startInterval,
			long stopInterval, List<AbstractTimelineObject> objects) {
		Map<AbstractTimelineObject, Double> output = new HashMap<>();

		double sum = objects.stream().mapToLong(t -> t.getDuration()).sum();
		objects.stream().forEach(e -> {
			output.put(e, usage * (e.getDuration() / sum));
		});

		return output;
	}

}
