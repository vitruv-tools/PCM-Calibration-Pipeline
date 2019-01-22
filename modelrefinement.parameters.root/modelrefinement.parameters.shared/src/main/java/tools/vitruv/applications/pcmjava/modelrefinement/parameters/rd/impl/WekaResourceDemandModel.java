package tools.vitruv.applications.pcmjava.modelrefinement.parameters.rd.impl;

import java.util.StringJoiner;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.WekaDataSet;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;

public class WekaResourceDemandModel implements ResourceDemandModel {

    private final LinearRegression classifier;
    private final WekaDataSet<Double> dataset;

    public WekaResourceDemandModel(final WekaDataSet<Double> dataset) throws Exception {
        this.dataset = dataset;
        
        this.classifier = new LinearRegression();
        this.classifier.buildClassifier(dataset.getDataSet());
    }

    
    public LinearRegression getClassifier() {
        return classifier;
    }


    public Instances getDataset() {
        return dataset.getDataSet();
    }


    @Override
    public double predictResourceDemand(final ServiceCall serviceCall) {
        Instance parametersInstance = this.dataset.buildTestInstance(serviceCall.getParameters());
        try {
            return this.classifier.classifyInstance(parametersInstance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getResourceDemandStochasticExpression() {
        StringJoiner result = new StringJoiner(" + (");
        double[] coefficients = this.classifier.coefficients();
        int braces = 0;
        for (int i = 0; i < coefficients.length - 2; i++) {
            if (coefficients[i] == 0.0) {
                continue;
            }
            StringBuilder coefficientPart = new StringBuilder();
            String paramStoEx = this.dataset.getStochasticExpressionForIndex(i);
            coefficientPart.append(coefficients[i]).append(" * ").append(paramStoEx);
            result.add(coefficientPart.toString());
            braces++;
        }
        result.add(String.valueOf(coefficients[coefficients.length - 1]));
        StringBuilder strBuilder = new StringBuilder().append(result.toString());
        for (int i = 0; i < braces; i++) {
            strBuilder.append(")");
        }
        return strBuilder.toString();
    }
}