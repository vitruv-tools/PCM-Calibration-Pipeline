package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.data;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.util.ServiceCallUtil;

public class ServiceCallBundle {
	private ServiceCall serviceCall;
	private int callCount;

	public ServiceCallBundle(ServiceCall call) {
		this.callCount = 1;
		this.serviceCall = call;
	}

	public boolean isLoop() {
		return this.callCount > 1;
	}

	public boolean canBeBundled(ServiceCallBundle bundle) {
		return this.serviceCall.getServiceId().equals(bundle.getServiceCall().getServiceId());
	}

	public boolean canBeBundled(ServiceCall id) {
		return this.serviceCall.getServiceId().equals(id.getServiceId());
	}

	public void bundle(ServiceCall other) {
		this.serviceCall = ServiceCallUtil.merge(this.serviceCall, other);
		this.callCount++;
	}

	public int getCallCount() {
		return this.callCount;
	}

	public ServiceCall getServiceCall() {
		return this.serviceCall;
	}

}
