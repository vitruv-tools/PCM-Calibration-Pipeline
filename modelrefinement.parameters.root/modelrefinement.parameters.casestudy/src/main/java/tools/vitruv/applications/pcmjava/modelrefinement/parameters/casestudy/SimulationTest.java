package tools.vitruv.applications.pcmjava.modelrefinement.parameters.casestudy;

import java.io.File;

import org.pcm.headless.api.client.PCMHeadlessClient;
import org.pcm.headless.api.client.SimulationClient;
import org.pcm.headless.shared.data.ESimulationType;
import org.pcm.headless.shared.data.config.HeadlessSimulationConfig;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.config.EPAPipelineConfiguration;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.InMemoryPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.LocalFilesystemPCM;

public class SimulationTest {

	public static void main(String[] args) {
		EPAPipelineConfiguration pipelineConfiguration = EPAPipelineConfiguration.fromFile(new File(
				"/Users/david/Desktop/Dynamic Approach/CIPM Migration/git/PCM-Calibration-Pipeline/modelrefinement.parameters.root/modelrefinement.parameters.casestudy/casestudy-data/config/pipeline.config.json"));

		LocalFilesystemPCM filesystemPCM = new LocalFilesystemPCM();
		filesystemPCM.setRepositoryFile(new File(pipelineConfiguration.getRepositoryPath()));
		filesystemPCM.setAllocationModelFile(new File(pipelineConfiguration.getAllocationModelPath()));
		filesystemPCM.setResourceEnvironmentFile(new File(pipelineConfiguration.getResourceEnvironmentModelPath()));
		filesystemPCM.setSystemFile(new File(pipelineConfiguration.getSystemPath()));
		filesystemPCM.setUsageModelFile(new File(pipelineConfiguration.getUsageModelPath()));

		InMemoryPCM pcm = InMemoryPCM.createFromFilesystem(filesystemPCM);

		SimulationClient client = new PCMHeadlessClient(pipelineConfiguration.getPcmBackendUrl()).prepareSimulation();

		client.setRepository(pcm.getRepository());
		client.setAllocation(pcm.getAllocationModel());
		client.setSystem(pcm.getSystem());
		client.setResourceEnvironment(pcm.getResourceEnvironmentModel());
		client.setUsageModel(pcm.getUsageModel());

		HeadlessSimulationConfig simConfig = HeadlessSimulationConfig.builder()
				.experimentName("Automatic Palladio Execution").repetitions(5).maximumMeasurementCount(10000)
				.simulationTime(300000).type(ESimulationType.SIMUCOM).build();

		client.setSimulationConfig(simConfig);

		client.createTransitiveClosure();
		client.sync();

		client.executeSimulation(r -> {

		});

	}

}
