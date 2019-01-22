package tools.vitruv.applications.pcmjava.modelrefinement.parameters.arguments;

import org.palladiosimulator.pcm.seff.ExternalCallAction;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;

/**
 * Interface for service argument prediction.
 * 
 * @author JP
 *
 */
public interface ArgumentPrediction {

    Object[] predictArguments(ExternalCallAction externalCall, ServiceCall serviceCall);

}