package tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.data;

import java.util.ArrayList;
import java.util.List;

public class ServiceResults {
	private List<ServiceResult> results;
	
	public ServiceResults() {
		this.results = new ArrayList<>();
	}

	public List<ServiceResult> getResults() {
		return results;
	}

	public void setResults(List<ServiceResult> results) {
		this.results = results;
	}
}
