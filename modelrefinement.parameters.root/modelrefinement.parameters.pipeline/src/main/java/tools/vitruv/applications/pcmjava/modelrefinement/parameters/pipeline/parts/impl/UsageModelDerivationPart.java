package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl;

import java.util.List;

import org.palladiosimulator.pcm.usagemodel.UsageModel;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.PipelineState;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.InMemoryPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.UsageModelExtractor;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.UsageScenarioBehaviourBuilder;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.cluster.SessionCluster;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.mapping.MonitoringDataMapping;

public class UsageModelDerivationPart extends AbstractPipelinePart {
	private MonitoringDataMapping mapping;

	public UsageModelDerivationPart(MonitoringDataMapping mapping) {
		super(false);
		this.mapping = mapping;
	}

	@Override
	protected void execute() {
		long usageModelStart = System.currentTimeMillis();
		logger.info("Deriving actual usage model.");
		getBlackboard().setState(PipelineState.USAGEMODEL_UPDATE);

		// load extractor
		InMemoryPCM currentPCM = getBlackboard().getLoadedPcm();
		UsageModelExtractor extractor = new UsageModelExtractor(currentPCM.getRepository(), currentPCM.getUsageModel(),
				currentPCM.getSystem());
		// TODO maybe make the values dynamically changeable
		List<SessionCluster> clusters = extractor.extractUserGroups(getBlackboard().getMonitoringData(), 0.8f, 5);

		// build new usagemodel
		UsageScenarioBehaviourBuilder builder = new UsageScenarioBehaviourBuilder(currentPCM.getSystem(),
				currentPCM.getRepository(), mapping);
		UsageModel result = builder.buildFullUsagemodel(clusters);

		logger.info("Usagemodel derivation needed " + (System.currentTimeMillis() - usageModelStart) + "ms");

		// save new usagemodel
		currentPCM.setUsageModel(result);
		getBlackboard().persistInMemoryPCM();
	}

}
