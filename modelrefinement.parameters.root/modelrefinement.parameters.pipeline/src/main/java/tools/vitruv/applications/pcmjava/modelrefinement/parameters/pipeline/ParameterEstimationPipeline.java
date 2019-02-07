package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.config.EPAPipelineConfiguration;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.LocalFilesystemPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl.CrossValidationPart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl.DockerImportMonitoringPart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl.LoadMonitoringDataPart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl.LoadPCMModelsPart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl.PalladioExecutorPart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl.ParameterEstimationPart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl.ResourceDemandEstimationPart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl.UsageModelDerivationPart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.mapping.MonitoringDataMapping;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class ParameterEstimationPipeline extends AbstractPCMPipeline {
	private Logger logger;

	private EPAPipelineConfiguration pipelineConfiguration;

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
			logger.error("Failed to build the EPS pipeline.");
			return;
		}

		// clean part
		this.addPart(new AbstractPipelinePart(false) {
			@Override
			protected void execute() {
				getBlackboard().setState(PipelineState.INIT);
			}
		});

		// initialization part
		this.addPart(new LoadPCMModelsPart(filesystemPCM));

		// perform palladio analysis before
		this.addPart(
				new PalladioExecutorPart(pipelineConfiguration.getJavaPath(), pipelineConfiguration.getEclipsePath()));

		// produce monitoring data (load test)
		// TODO inspect why load testing doesnt work
		// this.addPart(new
		// DockerCleanMonitoringPart(pipelineConfiguration.getDocker()));
		// this.addPart(new LoadTestingPart(pipelineConfiguration.getJmeterPath(),
		// pipelineConfiguration.getJmxPath()));
		this.addPart(new DockerImportMonitoringPart(pipelineConfiguration.getDocker(),
				pipelineConfiguration.getMonitoringDataPath()));

		// load current monitoring data
		this.addPart(new LoadMonitoringDataPart(pipelineConfiguration.getMonitoringDataPath()));

		// derive actual models
		// -> doesnt work atm, maybe need to cherry pick his changes
		this.addPart(new ParameterEstimationPart());

		// perform palladio before us
		this.addPart(
				new PalladioExecutorPart(pipelineConfiguration.getJavaPath(), pipelineConfiguration.getEclipsePath()));

		// do our job
		this.addPart(new ResourceDemandEstimationPart());
		this.addPart(new UsageModelDerivationPart(monitoringDataMapping));

		// perform palladio analysis
		this.addPart(
				new PalladioExecutorPart(pipelineConfiguration.getJavaPath(), pipelineConfiguration.getEclipsePath()));

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

		if (pipelineConfiguration.getJavaPath() == null) {
			pipelineConfiguration.setJavaPath(System.getProperty("java.home"));
		}
	}

}
