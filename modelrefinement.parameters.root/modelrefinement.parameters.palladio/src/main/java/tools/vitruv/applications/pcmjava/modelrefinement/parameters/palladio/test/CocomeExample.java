package tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.test;

import java.io.File;

import org.palladiosimulator.monitorrepository.MonitorRepository;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.servicelevelobjective.ServiceLevelObjectiveRepository;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class CocomeExample {

	public static UsageModel usage = PcmUtils.readFromFile(new File("cocome/cocome.usagemodel").getAbsolutePath(),
			UsageModel.class);
	public static Allocation allocation = PcmUtils.readFromFile(new File("cocome/cocome.allocation").getAbsolutePath(),
			Allocation.class);
	public static Repository repo = PcmUtils.readFromFile(new File("cocome/cocome.repository").getAbsolutePath(),
			Repository.class);
	public static org.palladiosimulator.pcm.system.System sys = PcmUtils.readFromFile(
			new File("cocome/cocome.system").getAbsolutePath(), org.palladiosimulator.pcm.system.System.class);
	public static ResourceEnvironment env = PcmUtils
			.readFromFile(new File("cocome/cocome.resourceenvironment").getAbsolutePath(), ResourceEnvironment.class);
	public static ServiceLevelObjectiveRepository slo_repo = PcmUtils
			.readFromFile(new File("cocome/cocome.slo").getAbsolutePath(), ServiceLevelObjectiveRepository.class);
	public static MonitorRepository monitorrepo = PcmUtils
			.readFromFile(new File("cocome/cocome.monitorrepository").getAbsolutePath(), MonitorRepository.class);
}
