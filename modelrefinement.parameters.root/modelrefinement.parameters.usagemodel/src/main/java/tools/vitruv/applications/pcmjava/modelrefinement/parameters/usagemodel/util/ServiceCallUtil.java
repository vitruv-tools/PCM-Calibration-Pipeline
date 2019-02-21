package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.util;

import java.util.Map.Entry;

import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.IntDistribution;

public class ServiceCallUtil {

	public static ServiceCall merge(ServiceCall a, ServiceCall b) {
		if (a.getServiceId().equals(b.getServiceId())) {
			// infer arguments from b to a
			for (Entry<String, Object> parameter : a.getParameters().getParameters().entrySet()) {
				if (b.getParameters().getParameters().containsKey(parameter.getKey())) {
					Object other = b.getParameters().getParameters().get(parameter.getKey());
					Object mine = parameter.getValue();

					if (mine instanceof Integer) {
						IntDistribution n = new IntDistribution();
						n.push((int) mine);
						mine = n;
					}
					if (mine instanceof IntDistribution) {
						if (other instanceof IntDistribution) {
							((IntDistribution) mine).push((IntDistribution) other);
						} else if (other instanceof Integer) {
							((IntDistribution) mine).push((int) other);
						}
					}

					a.getParameters().getParameters().put(parameter.getKey(), mine);
				}
			}

			return a;
		} else {
			// this should never happen because it is an invariant
			System.out.println(a.getServiceId() + " <> " + b.getServiceId());
			throw new RuntimeException();
		}
	}

	public static PCMRandomVariable buildLongLiteral(long value) {
		PCMRandomVariable ret = CoreFactory.eINSTANCE.createPCMRandomVariable();
		ret.setSpecification(String.valueOf(value));
		return ret;
	}

	public static PCMRandomVariable buildIntLiteral(int value) {
		PCMRandomVariable ret = CoreFactory.eINSTANCE.createPCMRandomVariable();
		ret.setSpecification(String.valueOf(value));
		return ret;
	}

}
