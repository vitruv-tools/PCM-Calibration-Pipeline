package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.palladiosimulator.pcm.core.PCMRandomVariable;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.data.ResourceDemandTriple;

public interface IResourceDemandModel {

	public void put(ResourceDemandTriple triple);

	public List<String> getDependentParameters(float thres);

	public Pair<PCMRandomVariable, Double[]> deriveStochasticExpression(float thres);

}
