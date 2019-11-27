package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.config.EPAPipelineConfiguration;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.LocalFilesystemPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl.CrossValidationPart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl.DockerCleanMonitoringPart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl.DockerImportMonitoringPart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl.LoadMonitoringDataPart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl.LoadPCMModelsPart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl.LoadTestingPart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl.PalladioExecutorPart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl.ResourceDemandEstimationPart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl.UsageModelDerivationPart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.mapping.MonitoringDataMapping;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class ParameterEstimationPipeline extends AbstractPCMPipeline {
	private static final String DEFAULT_PCM_URL = "http://127.0.0.1:8080/";

	private Logger logger;

	protected EPAPipelineConfiguration pipelineConfiguration;

	private LocalFilesystemPCM filesystemPCM;
	private MonitoringDataMapping monitoringDataMapping;

	public ParameterEstimationPipeline(EPAPipelineConfiguration pipelineConfiguration) {
		this.logger = Logger.getLogger(getClass());

		this.pipelineConfiguration = pipelineConfiguration;
		this.buildPipeline();
	}

	@Override
	protected void buildPipeline() {
		// replaod pcm models
		PcmUtils.loadPCMModels();

		try {
			processConfiguration();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Failed to build the EPS pipeline.");
			return;
		}

		// clean part
		this.addPart(new AbstractPipelinePart(false) {
			@Override
			protected void execute() {
				getBlackboard().setState(PipelineState.INIT);
				getBlackboard().getAnalysisResults().clear();
			}
		});

		// initialization part
		this.addPart(new LoadPCMModelsPart(filesystemPCM));

		if (pipelineConfiguration.isDockerImport()) {
			this.addPart(new DockerCleanMonitoringPart(pipelineConfiguration.getDocker()));
		}

		if (pipelineConfiguration.isLoadTesting()) {
			// produce monitoring data (load test)
			this.addPart(
					new LoadTestingPart(pipelineConfiguration.getJmeterPath(), pipelineConfiguration.getJmxPath()));
		}

		if (pipelineConfiguration.isDockerImport()) {
			this.addPart(new DockerImportMonitoringPart(pipelineConfiguration.getDocker(),
					pipelineConfiguration.getMonitoringDataPath()));
		}

		// load current monitoring data
		this.addPart(new LoadMonitoringDataPart(pipelineConfiguration.getMonitoringDataPath()));

		// perform palladio analysis before
		if (pipelineConfiguration.isAnalysisBefore()) {
			this.addPart(new PalladioExecutorPart(pipelineConfiguration.getPcmBackendUrl()));
		}

		// perform palladio before us
		if (pipelineConfiguration.isAnalysisPreCalibration()) {
			this.addPart(new PalladioExecutorPart(pipelineConfiguration.getPcmBackendUrl()));
		}

		// do our job
		this.addPart(new ResourceDemandEstimationPart());

		this.addPart(new UsageModelDerivationPart(monitoringDataMapping));

		// perform palladio analysis
		this.addPart(new PalladioExecutorPart(pipelineConfiguration.getPcmBackendUrl()));

		// check results
		this.addPart(new CrossValidationPart());
	}

	private void processConfiguration() throws IOException {
		filesystemPCM = new LocalFilesystemPCM();
		filesystemPCM.setRepositoryFile(new File(pipelineConfiguration.getRepositoryPath()));
		filesystemPCM.setAllocationModelFile(new File(pipelineConfiguration.getAllocationModelPath()));
		filesystemPCM.setResourceEnvironmentFile(new File(pipelineConfiguration.getResourceEnvironmentModelPath()));
		filesystemPCM.setSystemFile(new File(pipelineConfiguration.getSystemPath()));
		filesystemPCM.setUsageModelFile(new File(pipelineConfiguration.getUsageModelPath()));

		monitoringDataMapping = new ObjectMapper().readValue(new File(pipelineConfiguration.getMonitoringDataMapping()),
				MonitoringDataMapping.class);

		if (pipelineConfiguration.getPcmBackendUrl() == null) {
			pipelineConfiguration.setPcmBackendUrl(DEFAULT_PCM_URL);
		}
	}

}
