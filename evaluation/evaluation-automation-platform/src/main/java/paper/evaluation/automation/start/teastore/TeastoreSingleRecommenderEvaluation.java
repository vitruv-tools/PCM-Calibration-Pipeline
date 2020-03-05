package paper.evaluation.automation.start.teastore;

import java.io.File;

import org.pcm.headless.api.client.PCMHeadlessClient;
import org.pcm.headless.api.util.PCMUtil;

import paper.evaluation.automation.EvaluationAutomizer;
import paper.evaluation.automation.data.EvaluationData;
import paper.evaluation.automation.start.EvaluationConfig;

public class TeastoreSingleRecommenderEvaluation {

	public static void main(String[] argv) {
		PCMUtil.loadPCMModels();

		File repo = new File("casestudys/teastore/one_recommender_case/pcm/teastore.repository");
		File system = new File("casestudys/teastore/one_recommender_case/pcm/teastore.system");
		File usage = new File("casestudys/teastore/one_recommender_case/pcm/teastore.usagemodel");
		File resenv = new File("casestudys/teastore/one_recommender_case/pcm/teastore.resourceenvironment");
		File alloc = new File("casestudys/teastore/one_recommender_case/pcm/teastore.allocation");

		File valFolder = new File("casestudys/teastore/one_recommender_case/validation/");

		File rawOutput = new File("casestudys/teastore/one_recommender_case/results/raw/raw.json");

		EvaluationData data = new EvaluationData();
		data.setRepository(repo);
		data.setSysten(system);
		data.setResourceenv(resenv);
		data.setAllocation(alloc);
		data.setUsagemodel(usage);
		data.setValidationFolder(valFolder);
		data.setTargetService("_fgN6Z2BTEem3FetPjQjq2g");
		data.setOutputJsonFile(rawOutput);

		PCMHeadlessClient client = new PCMHeadlessClient(EvaluationConfig.SIMULATION_REST_URL);
		EvaluationAutomizer automizer = new EvaluationAutomizer(client, data);

		automizer.execute(100, false, EvaluationConfig.defaultConfig);
	}

}
