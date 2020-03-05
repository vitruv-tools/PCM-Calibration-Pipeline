package paper.evaluation.automation.data;

import java.io.File;

public class EvaluationData {
	private String targetService;

	private File repository;
	private File systen;
	private File usagemodel;
	private File allocation;
	private File resourceenv;

	private File validationFolder;

	private File outputJsonFile;

	public File getRepository() {
		return repository;
	}

	public void setRepository(File repository) {
		this.repository = repository;
	}

	public File getSysten() {
		return systen;
	}

	public void setSysten(File systen) {
		this.systen = systen;
	}

	public File getUsagemodel() {
		return usagemodel;
	}

	public void setUsagemodel(File usagemodel) {
		this.usagemodel = usagemodel;
	}

	public File getAllocation() {
		return allocation;
	}

	public void setAllocation(File allocation) {
		this.allocation = allocation;
	}

	public File getResourceenv() {
		return resourceenv;
	}

	public void setResourceenv(File resourceenv) {
		this.resourceenv = resourceenv;
	}

	public File getValidationFolder() {
		return validationFolder;
	}

	public void setValidationFolder(File validationFolder) {
		this.validationFolder = validationFolder;
	}

	public String getTargetService() {
		return targetService;
	}

	public void setTargetService(String targetService) {
		this.targetService = targetService;
	}

	public File getOutputJsonFile() {
		return outputJsonFile;
	}

	public void setOutputJsonFile(File outputJsonFile) {
		this.outputJsonFile = outputJsonFile;
	}

}
