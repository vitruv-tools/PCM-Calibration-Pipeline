package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.IResourceDemandEstimator;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.ResourceDemandEstimator;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;

public class ResourceDemandEstimationPart extends AbstractPipelinePart {

	public ResourceDemandEstimationPart() {
		super(false);
	}

	@Override
	protected void execute() {
		IResourceDemandEstimator estimation = new ResourceDemandEstimator(getBlackboard().getLoadedPcm());
		estimation.prepare(getBlackboard().getMonitoringData());
		estimation.derive();

		// save it
		getBlackboard().persistInMemoryPCM();
	}

}
