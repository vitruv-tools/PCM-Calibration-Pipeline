package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.timeline;

public abstract class AbstractTimelineObject {

	private long duration;
	private long start;

	public AbstractTimelineObject(long start, long duration) {
		this.duration = duration;
		this.start = start;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return this.start + this.duration;
	}

}
