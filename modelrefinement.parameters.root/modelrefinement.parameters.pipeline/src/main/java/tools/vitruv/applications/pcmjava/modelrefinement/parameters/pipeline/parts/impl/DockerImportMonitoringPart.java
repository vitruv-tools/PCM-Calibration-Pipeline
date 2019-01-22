package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.PipelineState;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.config.DockerConfiguration;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;

public class DockerImportMonitoringPart extends AbstractPipelinePart {
	/**
	 * The constant BUFFER_SIZE.
	 */
	private static final int BUFFER_SIZE = 4096;

	private DockerConfiguration docker;
	private String monitoringPath;

	public DockerImportMonitoringPart(DockerConfiguration docker, String monitoringPath) {
		super(false);

		this.docker = docker;
		this.monitoringPath = monitoringPath;
	}

	@Override
	protected void execute() {
		logger.info("Collecting monitoring data from docker.");

		getBlackboard().setState(PipelineState.DOCKER_IMPORT);

		// rm -rf "monitoring"
		try {
			FileUtils.deleteDirectory(new File(monitoringPath));
		} catch (IOException e) {
			logger.warn("Failed to clean monitoring data directory.");
		}

		// docker cp $container:/etc/monitoring "$path"
		new File(monitoringPath).mkdirs();
		DockerClient client = DefaultDockerClient.builder().uri(docker.getUrl()).build();

		try (final TarArchiveInputStream tarStream = new TarArchiveInputStream(
				client.archiveContainer(docker.getContainer(), docker.getPath()))) {
			TarArchiveEntry entry;
			while ((entry = tarStream.getNextTarEntry()) != null) {
				if (!entry.isDirectory()) {
					String fileName = filePart(entry.getName());

					File outputFile = new File(monitoringPath
							+ (monitoringPath.endsWith(File.separator) ? "" : File.separator) + fileName);
					extractFile(tarStream, outputFile);
				}
			}

			tarStream.close();
		} catch (Exception e) {
			logger.warn("Failed to get monitoring data from docker container.");
		}

		client.close();
	}

	private void extractFile(InputStream inputStream, File outFile) throws IOException {
		int count = -1;
		byte buffer[] = new byte[BUFFER_SIZE];
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile), BUFFER_SIZE);
		while ((count = inputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
			out.write(buffer, 0, count);
		}
		out.close();
	}

	private String filePart(String name) {
		int s = name.lastIndexOf(File.separatorChar);
		return s == -1 ? null : name.substring(s + 1);
	}

}
