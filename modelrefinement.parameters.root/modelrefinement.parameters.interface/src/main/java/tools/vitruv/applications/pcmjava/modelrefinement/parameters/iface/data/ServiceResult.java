package tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.data;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.validation.LongDistribution;

public class ServiceResult {
	private String componentName;
	private String seffId;

	private LongDistribution monitoringDistribution;
	private LongDistribution analysisDistribution;

	public ServiceResult(String seffName, String seffId, LongDistribution monitoringDistribution,
			LongDistribution analysisDistribution) {
		super();
		this.componentName = seffName;
		this.seffId = seffId;
		this.monitoringDistribution = monitoringDistribution;
		this.analysisDistribution = analysisDistribution;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String seffName) {
		this.componentName = seffName;
	}

	public String getSeffId() {
		return seffId;
	}

	public void setSeffId(String seffId) {
		this.seffId = seffId;
	}

	public LongDistribution getMonitoringDistribution() {
		return monitoringDistribution;
	}

	public void setMonitoringDistribution(LongDistribution monitoringDistribution) {
		this.monitoringDistribution = monitoringDistribution;
	}

	public LongDistribution getAnalysisDistribution() {
		return analysisDistribution;
	}

	public void setAnalysisDistribution(LongDistribution analysisDistribution) {
		this.analysisDistribution = analysisDistribution;
	}
}
