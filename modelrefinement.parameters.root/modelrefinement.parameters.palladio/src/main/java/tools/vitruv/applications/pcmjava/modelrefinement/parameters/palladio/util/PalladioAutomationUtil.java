package tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.util;

import org.apache.commons.lang3.tuple.Pair;
import org.palladiosimulator.metricspec.MetricDescription;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.ComposedStructure;
import org.palladiosimulator.pcm.core.composition.Connector;
import org.palladiosimulator.pcm.core.composition.RequiredDelegationConnector;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.pcm.headless.shared.data.results.MeasuringPointType;
import org.pcm.headless.shared.data.results.PlainMeasuringPoint;
import org.pcm.headless.shared.data.results.PlainMetricDescription;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.InMemoryPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

// TODO refactoring
public class PalladioAutomationUtil {

	public static Pair<ServiceEffectSpecification, AssemblyContext> getSeffByMeasuringPoint(InMemoryPCM pcm,
			PlainMeasuringPoint point, PlainMetricDescription desc) {
		if (!(MetricType.fromId(desc.getId()) == MetricType.RESPONSE_TIME)) {
			return null;
		}

		if (point.getType() == MeasuringPointType.ENTRY_LEVEL_CALL && point.getSourceIds().size() == 1) {
			EntryLevelSystemCall entryCall = PcmUtils.getElementById(pcm.getUsageModel(), EntryLevelSystemCall.class,
					point.getSourceIds().get(0));
			return PcmUtils.getSeffByProvidedRoleAndSignature(pcm.getSystem(),
					entryCall.getOperationSignature__EntryLevelSystemCall(),
					entryCall.getProvidedRole_EntryLevelSystemCall());
		} else if (point.getType() == MeasuringPointType.ASSEMBLY_OPERATION && point.getSourceIds().size() == 2) {
			// get belonging action
			AbstractAction belongingAction = PcmUtils.getElementById(pcm.getRepository(), AbstractAction.class,
					point.getSourceIds().get(1));
			// this stays until i exactly know if the assembly context is relevant or not
			AssemblyContext ctx = PcmUtils.getElementById(pcm.getSystem(), AssemblyContext.class,
					point.getSourceIds().get(0));
			if (belongingAction != null && ctx != null) {
				if (belongingAction instanceof ExternalCallAction) {
					return getSeffByAssemblySignature(ctx,
							((ExternalCallAction) belongingAction).getRole_ExternalService(),
							((ExternalCallAction) belongingAction).getCalledService_ExternalService());
				}
			}
		}

		return null;
	}

	public static Pair<ServiceEffectSpecification, AssemblyContext> getSeffByAssemblySignature(AssemblyContext ctx,
			OperationRequiredRole reqRole, OperationSignature sig) {
		AssemblyContext providing = getContextProvidingRole(ctx, reqRole);
		if (providing != null) {
			return Pair.of(PcmUtils
					.getObjects(providing.getEncapsulatedComponent__AssemblyContext(), ServiceEffectSpecification.class)
					.stream().filter(seff -> {
						return seff.getDescribedService__SEFF().getId().equals(sig.getId());
					}).findFirst().orElse(null), providing);
		}
		return null;
	}

	// TODO check the logic here don't know if its fully correct (see inner todo)
	public static AssemblyContext getContextProvidingRole(AssemblyContext ctx, OperationRequiredRole role) {
		ComposedStructure parentStructure = ctx.getParentStructure__AssemblyContext();
		for (Connector connector : parentStructure.getConnectors__ComposedStructure()) {
			if (connector instanceof AssemblyConnector) {
				AssemblyConnector assConnector = (AssemblyConnector) connector;
				if (assConnector.getRequiringAssemblyContext_AssemblyConnector().equals(ctx)) {
					if (assConnector.getProvidedRole_AssemblyConnector().getProvidedInterface__OperationProvidedRole()
							.getId().equals(role.getRequiredInterface__OperationRequiredRole().getId())) {
						return ((AssemblyConnector) connector).getProvidingAssemblyContext_AssemblyConnector();
					}
				}
			} else if (connector instanceof RequiredDelegationConnector && ((RequiredDelegationConnector) connector)
					.getOuterRequiredRole_RequiredDelegationConnector().equals(role)) {
				// TODO this branch is not tested
				AssemblyContext innerCtx = getContextProvidingRole(
						((RequiredDelegationConnector) connector).getAssemblyContext_RequiredDelegationConnector(),
						role);
				if (innerCtx != null) {
					return innerCtx;
				}
			}
		}

		return null;
	}

	public static MetricType getMetricType(MetricDescription desc) {
		return MetricType.fromId(desc.getId());
	}

}
