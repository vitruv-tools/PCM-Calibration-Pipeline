package tools.vitruv.applications.pcmjava.modelrefinement.parameters.casestudy;

import java.io.File;

import org.palladiosimulator.pcm.repository.Repository;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.SeffParameterEstimation;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class ParameterEstimationTest {

	public static void main(String[] args) {

		PcmUtils.loadPCMModels();

		MonitoringDataSet md = new KiekerMonitoringReader(new File("casestudy-data/monitoring").getAbsolutePath());

		Repository repo = PcmUtils.readFromFile(new File("casestudy-data/pcm/cocome.repository").getAbsolutePath(),
				Repository.class);

		SeffParameterEstimation estimation = new SeffParameterEstimation();
		estimation.update(repo, md);

	}

}
