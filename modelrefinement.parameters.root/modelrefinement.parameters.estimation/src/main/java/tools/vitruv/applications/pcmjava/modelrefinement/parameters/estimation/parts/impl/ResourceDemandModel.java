package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.data.ResourceDemandTriple;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.IResourceDemandModel;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class ResourceDemandModel implements IResourceDemandModel {
	private static final long NANO_TO_MS = 1000L * 1000L;

	private List<ResourceDemandTriple> data;

	public ResourceDemandModel() {
		this.data = new ArrayList<>();
	}

	@Override
	public void put(ResourceDemandTriple trip) {
		this.data.add(trip);
	}

	@Override
	public PCMRandomVariable deriveStochasticExpression(float thres) {
		LinearRegression regression = new LinearRegression();
		// get attributes
		List<String> attributes = getDependentParameters(thres);

		ArrayList<Attribute> wekaAttributes = new ArrayList<>();
		Map<String, Integer> attributeIndexMapping = new HashMap<>();
		Map<Integer, String> indexAttributeMapping = new HashMap<>();
		int k = 0;
		for (String stringAttribute : attributes) {
			wekaAttributes.add(new Attribute(stringAttribute));
			indexAttributeMapping.put(k, stringAttribute);
			attributeIndexMapping.put(stringAttribute, k++);
		}
		wekaAttributes.add(new Attribute("class"));

		Instances dataset = new Instances("dataset", wekaAttributes, 0);
		dataset.setClassIndex(dataset.numAttributes() - 1);

		for (ResourceDemandTriple triple : data) {
			double durationInMS = triple.getTime() / NANO_TO_MS;
			double[] values = new double[dataset.numAttributes()];
			for (Entry<String, Object> parameter : triple.getParameters().getParameters().entrySet()) {
				if (attributeIndexMapping.containsKey(parameter.getKey())) {
					int index = attributeIndexMapping.get(parameter.getKey());
					double value = resolveParameterValue(parameter.getValue());
					values[index] = value;
				}
			}

			values[dataset.numAttributes() - 1] = durationInMS;
			DenseInstance instance = new DenseInstance(1, values);
			dataset.add(instance);
		}

		// get coefficients
		try {
			regression.buildClassifier(dataset);
			double[] coeff = regression.coefficients();

			PCMRandomVariable var = CoreFactory.eINSTANCE.createPCMRandomVariable();
			var.setSpecification(getResourceDemandStochasticExpression(coeff, indexAttributeMapping));
			if (!var.getSpecification().contains("E")) {
				// TODO this is only a hotfix
				return var;
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public List<String> getDependentParameters(float thres) {
		Map<String, List<Pair<Double, Double>>> parameterMap = new HashMap<>();
		for (ResourceDemandTriple triple : data) {
			for (Entry<String, Object> parameter : triple.getParameters().getParameters().entrySet()) {
				double parameterValue = resolveParameterValue(parameter.getValue());

				if (!Double.isNaN(parameterValue)) {
					// add a pair
					if (!parameterMap.containsKey(parameter.getKey())) {
						parameterMap.put(parameter.getKey(), new ArrayList<>());
					}
					parameterMap.get(parameter.getKey()).add(Pair.of(parameterValue, triple.getTime()));
				}
			}
		}

		SpearmansCorrelation spCorr = new SpearmansCorrelation();
		List<String> retList = new ArrayList<>();
		for (Entry<String, List<Pair<Double, Double>>> entry : parameterMap.entrySet()) {
			double[] arr1 = entry.getValue().stream().mapToDouble(d -> d.getLeft()).toArray();
			double[] arr2 = entry.getValue().stream().mapToDouble(d -> d.getRight()).toArray();

			double corr = spCorr.correlation(arr1, arr2);
			if (!Double.isNaN(corr) && Math.abs(corr) >= thres) {
				// it is NaN when one variable is constant
				retList.add(entry.getKey());
			}
		}

		return retList;
	}

	private double resolveParameterValue(Object val) {
		double parameterValue = Double.NaN;
		if (val instanceof Integer) {
			parameterValue = (int) val;
		} else if (val instanceof Double) {
			parameterValue = (double) val;
		} else if (val instanceof Long) {
			parameterValue = (long) val;
		}

		return parameterValue;
	}

	private String getResourceDemandStochasticExpression(double[] coefficients, Map<Integer, String> parameterMapping) {
		StringJoiner result = new StringJoiner(" + (");
		int braces = 0;
		for (int i = 0; i < coefficients.length - 2; i++) {
			if (coefficients[i] == 0.0) {
				continue;
			}
			StringBuilder coefficientPart = new StringBuilder();
			String paramStoEx = parameterMapping.get(i);
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
