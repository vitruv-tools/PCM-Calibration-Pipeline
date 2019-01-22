package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;

public abstract class AbstractPCMPipeline {
	private Logger logger;
	protected DataBlackboard blackboard;

	private List<AbstractPipelinePart> parts;
	private int iteration;

	protected AbstractPCMPipeline() {
		this.parts = new LinkedList<>();
		this.logger = Logger.getLogger(getClass());
		this.blackboard = new DataBlackboard();
		iteration = 0;
	}

	protected void addPart(AbstractPipelinePart part) {
		part.setBlackboard(blackboard);
		this.parts.add(part);
	}

	public void run() {
		logger.info("Starting EPS Pipeline.");
		logger.info("[iteration = " + String.valueOf(iteration) + "]");

		for (AbstractPipelinePart part : parts) {
			if (part.active()) {
				part.run();
			}
		}
		iteration++;
	}

	protected abstract void buildPipeline();

}
