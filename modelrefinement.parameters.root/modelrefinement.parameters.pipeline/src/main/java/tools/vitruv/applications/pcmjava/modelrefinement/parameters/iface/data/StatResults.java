package tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.data;

public class StatResults {
	private String seffId;
	private String componentName;

	private double avgMonitoring;
	private double avgAnalysis;

	private double averageTest;
	private double ksTest;

	public double getAvgMonitoring() {
		return avgMonitoring;
	}

	public void setAvgMonitoring(double avgMonitoring) {
		this.avgMonitoring = avgMonitoring;
	}

	public double getAvgAnalysis() {
		return avgAnalysis;
	}

	public void setAvgAnalysis(double avgAnalysis) {
		this.avgAnalysis = avgAnalysis;
	}

	public double getAverageTest() {
		return averageTest;
	}

	public void setAverageTest(double averageTest) {
		this.averageTest = averageTest;
	}

	public double getKsTest() {
		return ksTest;
	}

	public void setKsTest(double ksTest) {
		this.ksTest = ksTest;
	}

	public String getSeffId() {
		return seffId;
	}

	public void setSeffId(String seffId) {
		this.seffId = seffId;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}
}
