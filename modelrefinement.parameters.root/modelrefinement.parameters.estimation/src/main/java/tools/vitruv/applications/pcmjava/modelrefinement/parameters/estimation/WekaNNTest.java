package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceParameters;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.records.LoopRecord;

public class WekaNNTest {

	public static void main(String[] args) {
		KiekerMonitoringReader reader = new KiekerMonitoringReader("monitoring/");

		List<Double> a = new ArrayList<>();
		List<Double> b = new ArrayList<>();

		for (String loopId : reader.getLoops().getLoopIds()) {
			List<LoopRecord> records = reader.getLoops().getLoopRecords(loopId);
			for (LoopRecord record : records) {
				ServiceCall parent = reader.getServiceCalls().getServiceCalls().stream()
						.filter(call -> call.getServiceExecutionId().equals(record.getServiceExecutionId())).findFirst()
						.orElse(null);

				if (parent != null) {
					ServiceParameters parameters = parent.getParameters();
					String parameterName = parameters.getParameters().entrySet().stream().filter(entry -> {
						return entry.getKey().endsWith("NUMBER_OF_ELEMENTS");
					}).findFirst().map(first -> first.getKey()).orElse(null);

					if (parameterName != null) {
						a.add(new Integer((int) parameters.getParameters().get(parameterName)).doubleValue());
						b.add(new Long(record.getLoopIterationCount()).doubleValue());
					}
				}
			}
		}

		if (a.size() >= 2 && b.size() >= 2) {
			SpearmansCorrelation correlation = new SpearmansCorrelation();
			System.out.println(correlation.correlation(a.stream().mapToDouble(d -> d).toArray(),
					b.stream().mapToDouble(d -> d).toArray()));
		}
	}

}
