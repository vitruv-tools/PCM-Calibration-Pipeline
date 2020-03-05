package paper.evaluation.automation.start.teastore;

import java.io.File;

import org.pcm.headless.api.client.PCMHeadlessClient;
import org.pcm.headless.api.util.PCMUtil;

import paper.evaluation.automation.EvaluationAutomizer;
import paper.evaluation.automation.data.EvaluationData;
import paper.evaluation.automation.start.EvaluationConfig;

public class TeastoreEvolutionScenarioEvaluation {

	public static void main(String[] args) {
		PCMUtil.loadPCMModels();

		// base paths
		File pcmBasePath = new File("casestudys/teastore/evolution/pcms/");
		File validationBasePath = new File("casestudys/teastore/evolution/validation/");

		for (int i = 0; i < EvaluationConfig.EVOLUTION_ITERATION_COUNT; i++) {
			java.lang.System.out.println("Iteration " + i + ":");
			java.lang.System.out.println("----------------------------------");
			File repo = new File(pcmBasePath, "iteration" + i + "/teastore.repository");
			File systemf = new File(pcmBasePath, "iteration" + i + "/teastore.system");
			File usagef = new File(pcmBasePath, "iteration" + i + "/teastore.usagemodel");
			File resenv = new File(pcmBasePath, "iteration" + i + "/teastore.resourceenvironment");
			File allocf = new File(pcmBasePath, "iteration" + i + "/teastore.allocation");

			// update usage model with validation data
			File monitoringFolderVal = new File(validationBasePath, "iteration" + i + "/");
			File rawOutput = new File("casestudys/teastore/evolution/results/raw/iteration" + i + "_raw.json");

			EvaluationData data = new EvaluationData();
			data.setRepository(repo);
			data.setSysten(systemf);
			data.setResourceenv(resenv);
			data.setAllocation(allocf);
			data.setUsagemodel(usagef);
			data.setValidationFolder(monitoringFolderVal);
			data.setTargetService("_fgN6Z2BTEem3FetPjQjq2g");
			data.setOutputJsonFile(rawOutput);

			PCMHeadlessClient client = new PCMHeadlessClient(EvaluationConfig.SIMULATION_REST_URL);
			EvaluationAutomizer automizer = new EvaluationAutomizer(client, data);

			automizer.execute(25, false, EvaluationConfig.defaultConfig);
		}
	}

}
