package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.config;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EPAPipelineConfiguration {

	public static EPAPipelineConfiguration fromFile(File jsonFile) {
		try {
			return new ObjectMapper().readValue(jsonFile, EPAPipelineConfiguration.class);
		} catch (IOException e) {
			return null;
		}
	}

	private boolean loadTesting;
	private boolean dockerImport;

	private String javaPath;
	private String eclipsePath;
	private String jmeterPath;

	private String repositoryPath;
	private String systemPath;
	private String usageModelPath;
	private String allocationModelPath;
	private String resourceEnvironmentModelPath;

	private String jmxPath;

	private String monitoringDataMapping;
	private String monitoringDataPath;

	private DockerConfiguration docker;

	private boolean analysisOnce;

	private boolean analysisBefore;
	private boolean analysisPreCalibration;

	public String getJavaPath() {
		return javaPath;
	}

	public void setJavaPath(String javaPath) {
		this.javaPath = javaPath;
	}

	public String getEclipsePath() {
		return eclipsePath;
	}

	public void setEclipsePath(String eclipsePath) {
		this.eclipsePath = eclipsePath;
	}

	public String getRepositoryPath() {
		return repositoryPath;
	}

	public void setRepositoryPath(String repositoryPath) {
		this.repositoryPath = repositoryPath;
	}

	public String getSystemPath() {
		return systemPath;
	}

	public void setSystemPath(String systemPath) {
		this.systemPath = systemPath;
	}

	public String getUsageModelPath() {
		return usageModelPath;
	}

	public void setUsageModelPath(String usageModelPath) {
		this.usageModelPath = usageModelPath;
	}

	public String getAllocationModelPath() {
		return allocationModelPath;
	}

	public void setAllocationModelPath(String allocationModelPath) {
		this.allocationModelPath = allocationModelPath;
	}

	public String getResourceEnvironmentModelPath() {
		return resourceEnvironmentModelPath;
	}

	public void setResourceEnvironmentModelPath(String resourceEnvironmentModelPath) {
		this.resourceEnvironmentModelPath = resourceEnvironmentModelPath;
	}

	public String getMonitoringDataPath() {
		return monitoringDataPath;
	}

	public void setMonitoringDataPath(String monitoringDataPath) {
		this.monitoringDataPath = monitoringDataPath;
	}

	public String getMonitoringDataMapping() {
		return monitoringDataMapping;
	}

	public void setMonitoringDataMapping(String monitoringDataMapping) {
		this.monitoringDataMapping = monitoringDataMapping;
	}

	public String getJmeterPath() {
		return jmeterPath;
	}

	public void setJmeterPath(String jmeterPath) {
		this.jmeterPath = jmeterPath;
	}

	public DockerConfiguration getDocker() {
		return docker;
	}

	public void setDocker(DockerConfiguration docker) {
		this.docker = docker;
	}

	public String getJmxPath() {
		return jmxPath;
	}

	public void setJmxPath(String jmxPath) {
		this.jmxPath = jmxPath;
	}

	public boolean isAnalysisOnce() {
		return analysisOnce;
	}

	public void setAnalysisOnce(boolean analysisOnce) {
		this.analysisOnce = analysisOnce;
	}

	public boolean isLoadTesting() {
		return loadTesting;
	}

	public void setLoadTesting(boolean loadTesting) {
		this.loadTesting = loadTesting;
	}

	public boolean isDockerImport() {
		return dockerImport;
	}

	public void setDockerImport(boolean dockerImport) {
		this.dockerImport = dockerImport;
	}

	public boolean isAnalysisBefore() {
		return analysisBefore;
	}

	public void setAnalysisBefore(boolean analysisBefore) {
		this.analysisBefore = analysisBefore;
	}

	public boolean isAnalysisPreCalibration() {
		return analysisPreCalibration;
	}

	public void setAnalysisPreCalibration(boolean analysisPreCalibration) {
		this.analysisPreCalibration = analysisPreCalibration;
	}
}
