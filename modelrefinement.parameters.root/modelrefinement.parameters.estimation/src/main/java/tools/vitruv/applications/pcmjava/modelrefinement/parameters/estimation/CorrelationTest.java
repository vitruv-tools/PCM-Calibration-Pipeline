package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceParameters;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.impl.KiekerMonitoringReader;

public class CorrelationTest {

	public static void main(String[] args) {
		KiekerMonitoringReader reader = new KiekerMonitoringReader("monitoring/");

		for (String serviceId : reader.getServiceCalls().getServiceIds()) {
			List<ServiceCall> calls = reader.getServiceCalls().getServiceCalls(serviceId);

			List<Double> a = new ArrayList<>();
			List<Double> b = new ArrayList<>();
			for (ServiceCall call : calls) {
				ServiceParameters parameters = call.getParameters();

				String parameterName = parameters.getParameters().entrySet().stream().filter(entry -> {
					return entry.getKey().endsWith("NUMBER_OF_ELEMENTS");
				}).findFirst().map(first -> first.getKey()).orElse(null);

				if (parameterName != null) {
					a.add(new Integer((int) parameters.getParameters().get(parameterName)).doubleValue());
					b.add(new Long(call.getExitTime() - call.getEntryTime()).doubleValue());
				}
			}

			if (a.size() >= 2 && b.size() >= 2) {
				SpearmansCorrelation correlation = new SpearmansCorrelation();
				System.out.println(correlation.correlation(a.stream().mapToDouble(d -> d).toArray(),
						b.stream().mapToDouble(d -> d).toArray()));
			}
		}

	}

}
