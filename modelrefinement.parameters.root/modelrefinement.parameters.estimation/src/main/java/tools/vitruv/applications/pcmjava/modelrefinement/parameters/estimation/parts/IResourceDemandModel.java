package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts;

import java.util.List;

import org.palladiosimulator.pcm.core.PCMRandomVariable;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.data.ResourceDemandTriple;

public interface IResourceDemandModel {

	public void put(ResourceDemandTriple triple);

	public List<String> getDependentParameters(float thres);

	public PCMRandomVariable deriveStochasticExpression(float thres);

}
