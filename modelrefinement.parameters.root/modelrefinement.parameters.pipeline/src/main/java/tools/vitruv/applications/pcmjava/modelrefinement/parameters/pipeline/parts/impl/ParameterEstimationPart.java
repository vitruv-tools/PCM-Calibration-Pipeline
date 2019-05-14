package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.SeffParameterEstimation;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.PipelineState;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;

public class ParameterEstimationPart extends AbstractPipelinePart {

	public ParameterEstimationPart() {
		super(false);
	}

	@Override
	protected void execute() {
		long paraBranchStart = System.currentTimeMillis();
		logger.info("Using the parameter estimation.");
		getBlackboard().setState(PipelineState.PARAMETER_UPDATES);
		// update parameters
		SeffParameterEstimation estimation = new SeffParameterEstimation();
		estimation.update(getBlackboard().getLoadedPcm().getRepository(), getBlackboard().getMonitoringData());

		// save it
		getBlackboard().persistInMemoryPCM();
		logger.info("JP derivation needed " + (System.currentTimeMillis() - paraBranchStart) + "ms.");
	}

}
