package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.IResourceDemandEstimator;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.ResourceDemandEstimatorAlternative;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;

public class ResourceDemandEstimationPart extends AbstractPipelinePart {

	public ResourceDemandEstimationPart() {
		super(false);
	}

	@Override
	protected void execute() {
		long timelineStart = System.currentTimeMillis();
		IResourceDemandEstimator estimation = new ResourceDemandEstimatorAlternative(getBlackboard().getLoadedPcm());
		estimation.prepare(getBlackboard().getMonitoringData());
		estimation.derive();

		logger.info("Timeline derivation needed " + (System.currentTimeMillis() - timelineStart) + "ms.");

		// save it
		getBlackboard().persistInMemoryPCM();
	}

}
