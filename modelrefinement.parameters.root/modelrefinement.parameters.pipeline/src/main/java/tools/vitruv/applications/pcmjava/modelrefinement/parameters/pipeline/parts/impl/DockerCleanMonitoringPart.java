package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ExecCreation;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.PipelineState;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.config.DockerConfiguration;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;

public class DockerCleanMonitoringPart extends AbstractPipelinePart {
	private DockerConfiguration docker;

	public DockerCleanMonitoringPart(DockerConfiguration docker) {
		super(false);

		this.docker = docker;
	}

	@Override
	protected void execute() {
		logger.info("Clean docker container.");

		getBlackboard().setState(PipelineState.DOCKER_CLEAN);

		DockerClient client = DefaultDockerClient.builder().uri(docker.getUrl()).build();

		String[] deleteCommand = new String[] { "rm", "-rf", docker.getPath() };
		String[] mkdirCommand = new String[] { "mkdir", docker.getPath() };

		try {
			ExecCreation exec = client.execCreate(docker.getContainer(), deleteCommand);
			ExecCreation exec2 = client.execCreate(docker.getContainer(), mkdirCommand);

			client.execStart(exec.id());
			client.execStart(exec2.id());
		} catch (DockerException | InterruptedException e) {
			logger.warn("Could not clean docker monitoring data.");
		}

		client.close();
	}

}
