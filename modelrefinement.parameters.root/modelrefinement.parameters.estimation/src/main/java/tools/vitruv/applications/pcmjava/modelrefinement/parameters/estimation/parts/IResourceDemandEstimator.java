package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;

public interface IResourceDemandEstimator {

	public void prepare(MonitoringDataSet data);

	void derive();

}
