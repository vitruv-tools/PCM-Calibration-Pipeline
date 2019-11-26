package tools.vitruv.applications.pcmjava.modelrefinement.parameters;

import org.pcm.headless.api.client.ISimulationResultListener;
import org.pcm.headless.api.client.PCMHeadlessClient;
import org.pcm.headless.api.client.SimulationClient;
import org.pcm.headless.shared.data.config.HeadlessSimulationConfig;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.InMemoryPCM;

public class PalladioHeadlessExecutor {

	private PCMHeadlessClient client;

	public PalladioHeadlessExecutor(String url) {
		this.client = new PCMHeadlessClient(url);
	}

	public void triggerSimulation(InMemoryPCM pcm, HeadlessSimulationConfig simConfig,
			ISimulationResultListener listener) {

		SimulationClient client = this.client.prepareSimulation();

		client.setRepository(pcm.getRepository());
		client.setAllocation(pcm.getAllocationModel());
		client.setSystem(pcm.getSystem());
		client.setResourceEnvironment(pcm.getResourceEnvironmentModel());
		client.setUsageModel(pcm.getUsageModel());

		client.setSimulationConfig(simConfig);

		client.createTransitiveClosure();
		client.sync();

		client.executeSimulation(listener);
	}

}
