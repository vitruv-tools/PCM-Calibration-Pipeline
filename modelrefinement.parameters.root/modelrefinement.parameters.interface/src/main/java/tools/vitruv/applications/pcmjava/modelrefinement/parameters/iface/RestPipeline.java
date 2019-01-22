package tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.IPipelineStateListener;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.ParameterEstimationPipeline;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.PipelineState;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.config.EPAPipelineConfiguration;

public class RestPipeline extends ParameterEstimationPipeline implements IPipelineStateListener {

	private RestInterface rest;

	public RestPipeline(RestInterface rest, EPAPipelineConfiguration pipelineConfiguration) {
		super(pipelineConfiguration);
		this.rest = rest;
		this.rest.setPipeline(this);

		this.blackboard.getListeners().add(this);

		// set init state
		blackboard.setState(PipelineState.INIT);
	}

	@Override
	public void onChange(PipelineState nState) {
		this.rest.setCurrentState(nState);
	}

}
