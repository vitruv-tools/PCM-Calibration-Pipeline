package paper.evaluation.automation.start;

import org.pcm.headless.shared.data.ESimulationType;
import org.pcm.headless.shared.data.config.HeadlessSimulationConfig;

public class EvaluationConfig {

	public static final int EVOLUTION_ITERATION_COUNT = 10;
	public static final String SIMULATION_REST_URL = "http://127.0.0.1:8080/";
	public static HeadlessSimulationConfig defaultConfig = HeadlessSimulationConfig.builder()
			.experimentName("Evaluation Paper").maximumMeasurementCount(30000).repetitions(1).simulationTime(150000 * 3)
			.type(ESimulationType.SIMUCOM).build();

}
