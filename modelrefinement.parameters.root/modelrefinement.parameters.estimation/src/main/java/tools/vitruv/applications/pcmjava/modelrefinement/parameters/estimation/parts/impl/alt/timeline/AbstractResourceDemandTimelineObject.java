package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.timeline;

public abstract class AbstractResourceDemandTimelineObject {

	private long duration;

	public AbstractResourceDemandTimelineObject(long duration) {
		this.duration = duration;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

}
