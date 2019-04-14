package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.timeline;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceParameters;

public class ServiceCallTimelineObject extends AbstractTimelineObject {

	private String serviceId;
	private ServiceParameters parameters;

	public ServiceCallTimelineObject(ServiceCall call) {
		super(call.getEntryTime(), call.getExitTime() - call.getEntryTime());

		this.serviceId = call.getServiceId();
		this.parameters = call.getParameters();
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public ServiceParameters getParameters() {
		return parameters;
	}

	public void setParameters(ServiceParameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return "SC [" + serviceId + "] - (" + this.getStart() + ", " + this.getDuration() + ")";
	}

}
