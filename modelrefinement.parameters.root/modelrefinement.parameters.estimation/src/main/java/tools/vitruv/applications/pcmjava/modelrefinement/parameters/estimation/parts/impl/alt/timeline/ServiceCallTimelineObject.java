package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.timeline;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;

public class ServiceCallTimelineObject extends AbstractResourceDemandTimelineObject {

	private String serviceId;

	public ServiceCallTimelineObject(ServiceCall call) {
		super(call.getExitTime() - call.getEntryTime());

		this.serviceId = call.getServiceId();
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

}
