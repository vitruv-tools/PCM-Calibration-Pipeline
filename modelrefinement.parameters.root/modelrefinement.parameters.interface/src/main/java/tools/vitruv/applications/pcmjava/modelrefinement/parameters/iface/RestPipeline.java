package tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface;

import java.util.List;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.data.ServiceResults;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.data.StatResults;
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

	public EPAPipelineConfiguration getConfig() {
		return this.pipelineConfiguration;
	}

	protected List<List<StatResults>> provideStatsJson() {
		return blackboard.getStatResults();
	}

	protected List<ServiceResults> provideResultsJson() {
		return blackboard.getServiceResults();
	}

}
