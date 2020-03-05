package paper.evaluation.automation.util;

import java.util.ArrayList;
import java.util.List;

public class EvaluationJsonData {

	private List<List<Double>> distributionsAnalysis;
	private List<List<Double>> distributionsMonitoring;

	public EvaluationJsonData() {
		this.distributionsAnalysis = new ArrayList<>();
		this.distributionsMonitoring = new ArrayList<>();
	}

	public List<List<Double>> getDistributionsAnalysis() {
		return distributionsAnalysis;
	}

	public void setDistributionsAnalysis(List<List<Double>> distributionsAnalysis) {
		this.distributionsAnalysis = distributionsAnalysis;
	}

	public List<List<Double>> getDistributionsMonitoring() {
		return distributionsMonitoring;
	}

	public void setDistributionsMonitoring(List<List<Double>> distributionsMonitoring) {
		this.distributionsMonitoring = distributionsMonitoring;
	}

}
