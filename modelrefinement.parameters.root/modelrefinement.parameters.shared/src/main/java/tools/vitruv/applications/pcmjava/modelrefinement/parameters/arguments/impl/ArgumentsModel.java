package tools.vitruv.applications.pcmjava.modelrefinement.parameters.arguments.impl;

import java.util.Optional;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;

public interface ArgumentsModel {

    Object[] predictArguments(ServiceCall serviceCall);

    String[] getArgumentStochasticExpressions();

}