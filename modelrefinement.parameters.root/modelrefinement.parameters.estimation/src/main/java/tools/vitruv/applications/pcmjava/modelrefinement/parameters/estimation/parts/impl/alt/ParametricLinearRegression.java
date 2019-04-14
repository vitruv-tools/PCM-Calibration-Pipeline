package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt;

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

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceParameters;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.util.DoubleDistribution;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class ParametricLinearRegression {

	private List<Pair<ServiceParameters, Double>> underlying;
	private float innerThres;
	private int prec;

	public ParametricLinearRegression(List<Pair<ServiceParameters, Double>> data, int precision,
			float dependencyThreshold) {
		this.underlying = data;
		this.innerThres = dependencyThreshold;
		this.prec = precision;
	}

	public PCMRandomVariable deriveStoex(Map<String, String> parameterMapping) {
		LinearRegression regression = new LinearRegression();
		// get attributes
		List<String> attributes = getDependentParameters(this.innerThres);

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

		for (Pair<ServiceParameters, Double> tuple : underlying) {
			double demand = tuple.getRight();
			double[] values = new double[dataset.numAttributes()];
			for (Entry<String, Object> parameter : tuple.getLeft().getParameters().entrySet()) {
				if (attributeIndexMapping.containsKey(parameter.getKey())) {
					int index = attributeIndexMapping.get(parameter.getKey());
					double value = resolveParameterValue(parameter.getValue());
					values[index] = value;
				}
			}

			values[dataset.numAttributes() - 1] = demand;
			DenseInstance instance = new DenseInstance(1, values);
			dataset.add(instance);
		}

		// get coefficients
		try {
			regression.buildClassifier(dataset);
			double[] coeff = regression.coefficients();
			DoubleDistribution constants = buildConstantDistribution(coeff, dataset);

			PCMRandomVariable var = CoreFactory.eINSTANCE.createPCMRandomVariable();
			var.setSpecification(getResourceDemandStochasticExpression(coeff, constants, indexAttributeMapping));
			if (var.getSpecification() == null) {
				return null;
			} else {
				return var;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private DoubleDistribution buildConstantDistribution(double[] coeff, Instances dataset) {
		DoubleDistribution distr = new DoubleDistribution(prec);

		for (Instance instance : dataset) {
			double sum = 0;
			for (int k = 1; k < coeff.length - 1; k++) {
				sum += coeff[k] * instance.value(k - 1);
			}
			double constValue = instance.value(dataset.numAttributes() - 1) - sum;
			distr.put(constValue);

			if (constValue >= 1000 * 60 * 60) {
				return null;
			}
		}

		return distr;
	}

	private List<String> getDependentParameters(float thres) {
		Map<String, List<Pair<Double, Double>>> parameterMap = new HashMap<>();
		for (Pair<ServiceParameters, Double> tuple : underlying) {
			for (Entry<String, Object> parameter : tuple.getLeft().getParameters().entrySet()) {
				double parameterValue = resolveParameterValue(parameter.getValue());

				if (!Double.isNaN(parameterValue)) {
					// add a pair
					if (!parameterMap.containsKey(parameter.getKey())) {
						parameterMap.put(parameter.getKey(), new ArrayList<>());
					}
					parameterMap.get(parameter.getKey()).add(Pair.of(parameterValue, tuple.getRight()));
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

	private String getResourceDemandStochasticExpression(double[] coefficients, DoubleDistribution constant,
			Map<Integer, String> parameterMapping) {
		if (constant == null) {
			return null;
		}

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
		result.add(constant.toStoex().getSpecification());
		StringBuilder strBuilder = new StringBuilder().append(result.toString());
		for (int i = 0; i < braces; i++) {
			strBuilder.append(")");
		}
		return strBuilder.toString();
	}

}
