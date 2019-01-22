package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.InMemoryPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

// TODO
// Have a set of preprocessors, processors etc and only execute them here
public class ParameterEstimation {
	private InMemoryPCM pcm;

	private Map<String, AssemblyContext> serviceCallAssembly;
	private Map<String, AssemblyContext> internalActionAssembly;

	public ParameterEstimation(InMemoryPCM pcm) {
		this.pcm = pcm;

		this.serviceCallAssembly = new HashMap<>();
		this.internalActionAssembly = new HashMap<>();
	}

	public void udpateRepository(MonitoringDataSet data) {
		// preprocessing
		preprocessAssemblys(data);
		preprocessInternalActions(data);

		// go to updating
		predictResourceDemands(data);
	}

	private void preprocessInternalActions(MonitoringDataSet data) {
		for (String internalActionId : data.getResponseTimes().getInternalActionIds()) {
			// TODO own method
			InternalAction belonging = PcmUtils.getElementById(pcm.getRepository(), InternalAction.class,
					internalActionId);
			if (belonging != null) {
				String seffId = belonging.getResourceDemandingBehaviour_AbstractAction().getId();
				internalActionAssembly.put(internalActionId, serviceCallAssembly.get(seffId));
			} else {
				System.out.println(
						"Correspondent for Internal Action with ID:\"" + internalActionId + "\" could not be found.");
			}
		}
	}

	private void predictResourceDemands(MonitoringDataSet data) {
		for (String internalActionId : internalActionAssembly.keySet()) {
			AssemblyContext assembly = internalActionAssembly.get(internalActionId);

			if (assembly != null) {
				// we can do it
				Set<String> resources = data.getResponseTimes().getResourceIds(internalActionId);
				AllocationContext allocation = pcm.getAllocationModel().getAllocationContexts_Allocation().stream()
						.filter(alloc -> {
							return alloc.getAssemblyContext_AllocationContext().getId().equals(assembly.getId());
						}).findFirst().orElse(null);

				if (allocation != null) {
					ResourceContainer container = allocation.getResourceContainer_AllocationContext();

					for (String usedResource : resources) {
						ProcessingResourceSpecification spec = container
								.getActiveResourceSpecifications_ResourceContainer().parallelStream()
								.filter(res -> res.getActiveResourceType_ActiveResourceSpecification().getId()
										.equals(usedResource))
								.findFirst().orElse(null);

						// TODO this is necessary because we dont have the pcm standard loaded
						// update this later no time atm
						if (container.getActiveResourceSpecifications_ResourceContainer().size() == 1) {
							spec = container.getActiveResourceSpecifications_ResourceContainer().get(0);

							System.out
									.println(spec.getActiveResourceType_ActiveResourceSpecification().getEntityName());
						}
					}
				}
			}
		}
	}

	private void preprocessAssemblys(MonitoringDataSet data) {
		for (String serviceId : data.getServiceCalls().getServiceIds()) {
			// TODO own method (not here) and also this needs to be in the monitoring!
			ResourceDemandingSEFF seff = PcmUtils.getElementById(pcm.getRepository(), ResourceDemandingSEFF.class,
					serviceId);
			BasicComponent ownComponent = seff.getBasicComponent_ServiceEffectSpecification();

			AssemblyContext belongingCtx = PcmUtils
					.getObjects(pcm.getSystem(), AssemblyContext.class).stream().filter(assembly -> assembly
							.getEncapsulatedComponent__AssemblyContext().getId().equals(ownComponent.getId()))
					.findFirst().orElse(null);

			serviceCallAssembly.put(serviceId, belongingCtx);
		}
	}

}
