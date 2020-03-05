package paper.evaluation.automation.start.teastore;

import java.io.File;

import org.pcm.headless.api.client.PCMHeadlessClient;
import org.pcm.headless.api.util.PCMUtil;

import paper.evaluation.automation.EvaluationAutomizer;
import paper.evaluation.automation.data.EvaluationData;
import paper.evaluation.automation.start.EvaluationConfig;

public class TeastoreParameterizedEvaluation {
	private static File validationBaseFolder = new File("casestudys/teastore/parameterized_case/validation/");

	private static File nonParameterizedBaseFolder = new File(
			"casestudys/teastore/parameterized_case/pcms/non-parameterized/");
	private static File parameterizedBaseFolder = new File(
			"casestudys/teastore/parameterized_case/pcms/parameterized/");
	private static File resultsBaseFolder = new File("casestudys/teastore/parameterized_case/results/raw/");

	public static void main(String[] argv) {
		PCMUtil.loadPCMModels();

		execute20UserParameterized();
		execute20UserNonParameterized();

		execute40UserParameterized();
		execute40UserNonParameterized();
	}

	private static void execute40UserNonParameterized() {
		File validationFolder = new File(validationBaseFolder, "40user/");
		executeExperiment(nonParameterizedBaseFolder, validationFolder, resultsBaseFolder, "teastore40user.usagemodel",
				"non_parameterized_40user.json");
	}

	private static void execute20UserNonParameterized() {
		File validationFolder = new File(validationBaseFolder, "20user/");
		executeExperiment(nonParameterizedBaseFolder, validationFolder, resultsBaseFolder, "teastore20user.usagemodel",
				"non_parameterized_20user.json");
	}

	private static void execute40UserParameterized() {
		File validationFolder = new File(validationBaseFolder, "40user/");
		executeExperiment(parameterizedBaseFolder, validationFolder, resultsBaseFolder, "teastore40user.usagemodel",
				"parameterized_40user.json");
	}

	private static void execute20UserParameterized() {
		File validationFolder = new File(validationBaseFolder, "20user/");
		executeExperiment(parameterizedBaseFolder, validationFolder, resultsBaseFolder, "teastore20user.usagemodel",
				"parameterized_20user.json");
	}

	private static void executeExperiment(File pcmBaseFolder, File validationFolder, File resultsFolder,
			String usageProfile, String resultName) {
		File repo = new File(pcmBaseFolder, "teastore.repository");
		File system = new File(pcmBaseFolder, "teastore.system");
		File usage = new File(pcmBaseFolder, usageProfile);
		File resenv = new File(pcmBaseFolder, "teastore.resourceenvironment");
		File alloc = new File(pcmBaseFolder, "teastore.allocation");

		File resultFile = new File(resultsFolder, resultName);

		EvaluationData data = new EvaluationData();
		data.setRepository(repo);
		data.setSysten(system);
		data.setResourceenv(resenv);
		data.setAllocation(alloc);
		data.setUsagemodel(usage);
		data.setValidationFolder(validationFolder);
		data.setTargetService("_fgN6Z2BTEem3FetPjQjq2g");
		data.setOutputJsonFile(resultFile);

		PCMHeadlessClient client = new PCMHeadlessClient(EvaluationConfig.SIMULATION_REST_URL);
		EvaluationAutomizer automizer = new EvaluationAutomizer(client, data);

		automizer.execute(25, false, EvaluationConfig.defaultConfig);
	}

}
