package tools.vitruv.applications.pcmjava.modelrefinement.parameters.arguments.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.GuardedBranchTransition;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCallDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.arguments.ArgumentEstimation;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.arguments.ArgumentPrediction;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.branch.BranchDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.branch.BranchEstimation;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.branch.BranchPrediction;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class ArgumentEstimationImpl implements ArgumentEstimation, ArgumentPrediction {

    private static final Logger LOGGER = Logger.getLogger(ArgumentEstimationImpl.class);

    private final Map<String, ArgumentsModel> modelCache;
    private final Random random;

    public ArgumentEstimationImpl() {
        this(ThreadLocalRandom.current());
    }

    public ArgumentEstimationImpl(final Random random) {
        this.modelCache = new HashMap<>();
        this.random = random;
    }

    @Override
    public Object[] predictArguments(ExternalCallAction externalCall, ServiceCall serviceCall) {
        ArgumentsModel argumentsModel = this.modelCache.get(externalCall.getId());
        if (argumentsModel == null) {
            throw new IllegalArgumentException(
                    "An estimation for external call with id " + externalCall.getId() + " was not found.");
        }
        return argumentsModel.predictArguments(serviceCall);
    }

    @Override
    public void update(Repository pcmModel, ServiceCallDataSet serviceCalls) {

        this.applyEstimations(pcmModel);
    }

    private void applyEstimations(final Repository pcmModel) {
        List<ExternalCallAction> externalCalls = PcmUtils.getObjects(pcmModel, ExternalCallAction.class);
        for (ExternalCallAction externalCall : externalCalls) {
            this.applyModel(externalCall);
        }
    }

    private void applyModel(final ExternalCallAction externalCall) {
        EList<Parameter> serviceArguments = externalCall.getCalledService_ExternalService()
                .getParameters__OperationSignature();

    }

}
