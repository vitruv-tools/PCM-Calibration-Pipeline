package tools.vitruv.applications.pcmjava.modelrefinement.parameters.rd.impl;

import java.util.Map;
import java.util.Map.Entry;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceParameters;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.WekaDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.WekaDataSetBuilder;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.WekaDataSetMode;

/**
 * Implements the resource demand parametric dependency estimation by using linear regression from the weka library.
 * This does not imply that only linear dependencies can be detected, because we present different pre-defined possible
 * dependency relations, such as a quadratic dependency, as input. The linear regression then finds the best candidates.
 * 
 * @author JP
 *
 */
public class WekaParametricDependencyEstimationStrategy implements ParametricDependencyEstimationStrategy {

    @Override
    public ResourceDemandModel estimateResourceDemandModel(final String internalActionId, final String resourceId,
            final Map<ServiceParameters, Double> resourceDemands) {
        try {
            return this.internEstimateResourceDemandModel(internalActionId, resourceId, resourceDemands);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ResourceDemandModel internEstimateResourceDemandModel(final String internalActionId,
            final String resourceId,
            final Map<ServiceParameters, Double> resourceDemands) throws Exception {

        // If no service parameters are monitored, we have a constant resource demand.
        if (resourceDemands.size() == 1) {
            double singleValue = resourceDemands.values().iterator().next();
            return new ConstantResourceDemandModel(singleValue);
        }

        WekaDataSetBuilder<Double> dataSetBuilder = new WekaDataSetBuilder<Double>(WekaDataSetMode.NumericOnly);
        
        for (Entry<ServiceParameters, Double> rdEntry : resourceDemands.entrySet()) {
            dataSetBuilder.addInstance(rdEntry.getKey(), rdEntry.getValue());
        }

        WekaDataSet<Double> dataset = dataSetBuilder.build();
        return new WekaResourceDemandModel(dataset);
    }

    private static class ConstantResourceDemandModel implements ResourceDemandModel {

        private final double resourceDemand;

        public ConstantResourceDemandModel(final double resourceDemand) {
            this.resourceDemand = resourceDemand;
        }

        @Override
        public double predictResourceDemand(final ServiceCall serviceCall) {
            return this.resourceDemand;
        }

        @Override
        public String getResourceDemandStochasticExpression() {
            return String.valueOf(this.resourceDemand);
        }
    }

}
