package paper.evaluation.automation.start.cocome;

import java.io.File;

import org.pcm.headless.api.client.PCMHeadlessClient;
import org.pcm.headless.api.util.PCMUtil;

import paper.evaluation.automation.EvaluationAutomizer;
import paper.evaluation.automation.data.EvaluationData;
import paper.evaluation.automation.start.EvaluationConfig;

public class CocomeEvaluation {

	public static void main(String[] argv) {
		PCMUtil.loadPCMModels();

		File repo = new File("casestudys/cocome/pcm/cocome.repository");
		File system = new File("casestudys/cocome/pcm/cocome.system");
		File usage = new File("casestudys/cocome/pcm/cocome.usagemodel");
		File resenv = new File("casestudys/cocome/pcm/cocome.resourceenvironment");
		File alloc = new File("casestudys/cocome/pcm/cocome.allocation");

		File valFolder = new File("casestudys/cocome/validation/");

		EvaluationData data = new EvaluationData();
		data.setRepository(repo);
		data.setSysten(system);
		data.setResourceenv(resenv);
		data.setAllocation(alloc);
		data.setUsagemodel(usage);
		data.setValidationFolder(valFolder);
		data.setTargetService("bookSale");

		PCMHeadlessClient client = new PCMHeadlessClient("http://127.0.0.1:8080/");
		EvaluationAutomizer automizer = new EvaluationAutomizer(client, data);

		automizer.execute(100, false, EvaluationConfig.defaultConfig);
	}

}
