package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.timeline;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.records.ResponseTimeRecord;

public class InternalActionTimelineObject extends AbstractTimelineObject {

	private String internalActionId;

	public InternalActionTimelineObject(ResponseTimeRecord resp) {
		super(resp.getStartTime(), resp.getStopTime() - resp.getStartTime());

		this.internalActionId = resp.getInternalActionId();
	}

	public String getInternalActionId() {
		return internalActionId;
	}

	public void setInternalActionId(String internalActionId) {
		this.internalActionId = internalActionId;
	}

	@Override
	public String toString() {
		return "IA [" + internalActionId + "] - (" + this.getStart() + ", " + this.getDuration() + ")";
	}

}
