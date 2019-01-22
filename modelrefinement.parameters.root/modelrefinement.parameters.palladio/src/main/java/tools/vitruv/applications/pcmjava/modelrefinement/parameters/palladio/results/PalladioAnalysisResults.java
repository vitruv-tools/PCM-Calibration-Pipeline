package tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.results;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.measure.Measure;
import javax.measure.quantity.Duration;

import org.palladiosimulator.edp2.models.measuringpoint.MeasuringPoint;
import org.palladiosimulator.metricspec.MetricDescription;

public class PalladioAnalysisResults {
	private Map<MeasuringPoint, MeasuringPointResults> results;

	public PalladioAnalysisResults() {
		this.results = new HashMap<>();
	}

	public Set<Entry<MeasuringPoint, MeasuringPointResults>> entries() {
		return results.entrySet();
	}

	public void addLongs(MeasuringPoint p, List<Measure<Long, Duration>> longs, MetricDescription metric) {
		init(p, metric);
		this.results.get(p).applyLongValues(longs);
	}

	public void addDoubles(MeasuringPoint p, List<Measure<Double, Duration>> doubles, MetricDescription metric) {
		init(p, metric);
		this.results.get(p).applyDoubleValues(doubles);
	}

	public Map<MeasuringPoint, MeasuringPointResults> getResults() {
		return results;
	}

	private void init(MeasuringPoint p, MetricDescription metric) {
		if (!this.results.containsKey(p)) {
			this.results.put(p, new MeasuringPointResults(metric));
		}
	}

}
