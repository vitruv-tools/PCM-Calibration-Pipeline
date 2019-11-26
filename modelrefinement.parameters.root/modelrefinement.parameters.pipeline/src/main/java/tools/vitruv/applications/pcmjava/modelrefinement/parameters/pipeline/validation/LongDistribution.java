package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

public class LongDistribution {

	private List<Long> values;

	public LongDistribution() {
		this.values = new ArrayList<>();
	}

	public void addValue(long val) {
		values.add(val);
	}

	public double ksTest(LongDistribution other) {
		Collections.sort(other.values);
		Collections.sort(this.values);

		KolmogorovSmirnovTest test = new KolmogorovSmirnovTest();
		return test.kolmogorovSmirnovStatistic(this.values.stream().mapToDouble(l -> (double) l).toArray(),
				other.values.stream().mapToDouble(l -> (double) l).toArray());
	}

	public double avgTest(LongDistribution other) {
		double avg1 = this.values.stream().mapToDouble(t -> t.doubleValue()).average().getAsDouble();
		double avg2 = other.values.stream().mapToDouble(t -> t.doubleValue()).average().getAsDouble();

		double avgDistance = Math.abs(avg2 - avg1);
		return 1 - (avgDistance / Math.max(avg1, avg2));
	}

	public double avg() {
		return values.stream().mapToDouble(l -> (double) l).average().getAsDouble();
	}

	public List<Long> getValues() {
		return values;
	}

	public void setValues(List<Long> values) {
		this.values = values;
	}

	public int size() {
		return values.size();
	}

}
