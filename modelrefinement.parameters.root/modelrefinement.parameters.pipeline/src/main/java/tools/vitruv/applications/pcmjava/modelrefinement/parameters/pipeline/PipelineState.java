package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline;

public enum PipelineState {
	STARTED("STARTED"), FINISHED("FINISHED"), INIT("INIT"), DOCKER_CLEAN("DOCKER_CLEAN"), DOCKER_IMPORT(
			"DOCKER_IMPORT"), PCM_LOADED("PCM_LOADED"), LOAD_TESTING("LOAD_TESTING"), LOAD_MONITORING_DATA(
					"LOAD_MONITORING_DATA"), PCM_ANALYSIS("PCM_ANALYSIS"), PARAMETER_UPDATES(
							"PARAMETER_UPDATES"), USAGEMODEL_UPDATE("USAGEMODEL_UPDATE"), EVALUATION("EVALUATION");

	private String value;

	private PipelineState(String val) {
		this.value = val;
	}

	@Override
	public String toString() {
		return value;
	}
}
