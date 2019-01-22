package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data;

import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class InMemoryPCM {
	private Repository repository;
	private System system;
	private UsageModel usageModel;
	private Allocation allocationModel;
	private ResourceEnvironment resourceEnvironmentModel;

	public static InMemoryPCM createFromFilesystem(LocalFilesystemPCM pcm) {
		InMemoryPCM ret = new InMemoryPCM();
		ret.repository = PcmUtils.readFromFile(pcm.getRepositoryFile().getAbsolutePath(), Repository.class);
		ret.allocationModel = PcmUtils.readFromFile(pcm.getAllocationModelFile().getAbsolutePath(), Allocation.class);
		ret.resourceEnvironmentModel = PcmUtils.readFromFile(pcm.getResourceEnvironmentFile().getAbsolutePath(),
				ResourceEnvironment.class);
		ret.system = PcmUtils.readFromFile(pcm.getSystemFile().getAbsolutePath(), System.class);
		ret.usageModel = PcmUtils.readFromFile(pcm.getUsageModelFile().getAbsolutePath(), UsageModel.class);
		return ret;
	}

	public void saveToFilesystem(LocalFilesystemPCM pcm) {
		PcmUtils.saveToFile(this.getRepository(), pcm.getRepositoryFile().getAbsolutePath());
		PcmUtils.saveToFile(this.getAllocationModel(), pcm.getAllocationModelFile().getAbsolutePath());
		PcmUtils.saveToFile(this.getResourceEnvironmentModel(), pcm.getResourceEnvironmentFile().getAbsolutePath());
		PcmUtils.saveToFile(this.getSystem(), pcm.getSystemFile().getAbsolutePath());
		PcmUtils.saveToFile(this.getUsageModel(), pcm.getUsageModelFile().getAbsolutePath());
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public System getSystem() {
		return system;
	}

	public void setSystem(System system) {
		this.system = system;
	}

	public UsageModel getUsageModel() {
		return usageModel;
	}

	public void setUsageModel(UsageModel usageModel) {
		this.usageModel = usageModel;
	}

	public Allocation getAllocationModel() {
		return allocationModel;
	}

	public void setAllocationModel(Allocation allocationModel) {
		this.allocationModel = allocationModel;
	}

	public ResourceEnvironment getResourceEnvironmentModel() {
		return resourceEnvironmentModel;
	}

	public void setResourceEnvironmentModel(ResourceEnvironment resourceEnvironmentModel) {
		this.resourceEnvironmentModel = resourceEnvironmentModel;
	}
}
