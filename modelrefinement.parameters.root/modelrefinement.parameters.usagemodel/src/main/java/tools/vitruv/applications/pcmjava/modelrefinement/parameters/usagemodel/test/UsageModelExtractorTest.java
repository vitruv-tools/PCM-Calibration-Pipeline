package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.test;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.UsageModelExtractor;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.UsageScenarioBehaviourBuilder;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.cluster.SessionCluster;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.mapping.MonitoringDataMapping;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class UsageModelExtractorTest {

	@Test
	public void test() {
		PcmUtils.loadPCMModels();

		Repository pcmModel = PcmUtils.readFromFile(new File("cocome/cocome.repository").getAbsolutePath(),
				Repository.class);

		java.lang.System.out.println(
				PcmUtils.getElementById(pcmModel, InternalAction.class, "_2GlXMNL-EdujoZKiiOMQBA").getEntityName());

		System pcmSystem = PcmUtils.readFromFile(new File("cocome/cocome.system").getAbsolutePath(), System.class);
		UsageModel usage = PcmUtils.readFromFile(new File("cocome/cocome.usagemodel").getAbsolutePath(),
				UsageModel.class);
		MonitoringDataSet data = new KiekerMonitoringReader(
				"monitoring-data/monitoring/kieker-20181216-223438-27519603339170-UTC--KIEKER");
		MonitoringDataMapping mapping = new MonitoringDataMapping();
		mapping.addParameterMapping("products.VALUE", "saleTO.NUMBER_OF_ELEMENTS");

		UsageModelExtractor extractor = new UsageModelExtractor(pcmModel, usage, pcmSystem);
		List<SessionCluster> clusters = extractor.extractUserGroups(data, 0.8f, 5);

		UsageScenarioBehaviourBuilder builder = new UsageScenarioBehaviourBuilder(pcmSystem, pcmModel, mapping);
		UsageModel result = builder.buildFullUsagemodel(clusters);

		PcmUtils.saveToFile(result, "test.usagemodel");
	}

}
