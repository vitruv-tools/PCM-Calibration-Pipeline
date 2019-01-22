package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.data.usage;

import java.util.Map.Entry;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.parameter.ParameterFactory;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.parameter.VariableCharacterisationType;
import org.palladiosimulator.pcm.parameter.VariableUsage;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

import de.uka.ipd.sdq.stoex.StoexFactory;
import de.uka.ipd.sdq.stoex.VariableReference;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.mapping.MonitoringDataMapping;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.util.IntDistribution;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.util.ServiceCallUtil;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class UsageCallStructure extends AbstractUsageElement {
	private ServiceCall reference;

	public UsageCallStructure(ServiceCall reference) {
		this.reference = reference;
	}

	@Override
	public void merge(AbstractUsageElement other) {
		if (other instanceof UsageCallStructure) {
			this.reference = ServiceCallUtil.merge(this.reference, ((UsageCallStructure) other).reference);
		}
	}

	@Override
	public AbstractUserAction toUserAction(System sys, Repository repo, MonitoringDataMapping mapping) {
		// get seff
		ResourceDemandingSEFF seff = PcmUtils.getElementById(repo, ResourceDemandingSEFF.class,
				reference.getServiceId());

		// build entry call
		EntryLevelSystemCall entryCall = UsagemodelFactory.eINSTANCE.createEntryLevelSystemCall();
		entryCall.setOperationSignature__EntryLevelSystemCall((OperationSignature) seff.getDescribedService__SEFF());
		entryCall.setProvidedRole_EntryLevelSystemCall(PcmUtils.getProvidedRole(sys, seff));

		// parameter build
		if (reference.getParameters() != null) {
			for (Entry<String, Object> parameter : reference.getParameters().getParameters().entrySet()) {
				if (mapping.hasSEFFParameterName(parameter.getKey())) {
					// we need to build the parameter
					String belongingSeffParameter = mapping.getSEFFParameterName(parameter.getKey());
					String[] operatorSplit = belongingSeffParameter.split("\\.");

					VariableUsage usage = ParameterFactory.eINSTANCE.createVariableUsage();
					VariableCharacterisation character = ParameterFactory.eINSTANCE.createVariableCharacterisation();
					VariableReference reference = StoexFactory.eINSTANCE.createVariableReference();

					reference.setReferenceName(operatorSplit[0]);
					character.setType(VariableCharacterisationType.get(operatorSplit[1]));
					character.setSpecification_VariableCharacterisation(
							buildPCMVariable(operatorSplit[1], parameter.getValue()));
					usage.setNamedReference__VariableUsage(reference);
					usage.getVariableCharacterisation_VariableUsage().add(character);

					entryCall.getInputParameterUsages_EntryLevelSystemCall().add(usage);
				}
			}
		}

		return entryCall;
	}

	private PCMRandomVariable buildPCMVariable(String key, Object value) {
		if (value instanceof IntDistribution) {
			return ((IntDistribution) value).toStochasticExpression();
		} else if (value instanceof Integer) {
			return ServiceCallUtil.buildIntLiteral((int) value);
		}
		return null;
	}

	@Override
	public boolean matches(AbstractUsageElement b) {
		if (b == null)
			return false;
		if (b instanceof UsageCallStructure)
			return reference.getServiceId().equals(((UsageCallStructure) b).reference.getServiceId());

		return false;
	}
}
