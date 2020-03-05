package paper.evaluation.automation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.DoubleStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.ml.distance.EarthMoversDistance;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.pcm.headless.api.client.PCMHeadlessClient;
import org.pcm.headless.api.client.SimulationClient;
import org.pcm.headless.api.util.ModelUtil;
import org.pcm.headless.api.util.PCMUtil;
import org.pcm.headless.shared.data.config.HeadlessSimulationConfig;
import org.pcm.headless.shared.data.results.AbstractMeasureValue;
import org.pcm.headless.shared.data.results.DoubleMeasureValue;
import org.pcm.headless.shared.data.results.InMemoryResultRepository;
import org.pcm.headless.shared.data.results.LongMeasureValue;
import org.pcm.headless.shared.data.results.MeasuringPointType;
import org.pcm.headless.shared.data.results.PlainDataMeasure;
import org.pcm.headless.shared.data.results.PlainDataSeries;

import com.fasterxml.jackson.databind.ObjectMapper;

import paper.evaluation.automation.data.EvaluationData;
import paper.evaluation.automation.util.EvaluationJsonData;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.impl.KiekerMonitoringReader;

public class EvaluationAutomizer {
	private static final String METRIC_RESPONSE_TIME = "_mZb3MdoLEeO-WvSDaR6unQ";

	private PCMHeadlessClient rest;
	private EvaluationData data;

	private Percentile q1 = new Percentile(25);
	private Percentile q2 = new Percentile(50);
	private Percentile q3 = new Percentile(75);
	private Min minF = new Min();
	private Max maxF = new Max();
	private Mean meanF = new Mean();

	private PCMElementIDCache<EntryLevelSystemCall> entryCallCache = new PCMElementIDCache<>(
			EntryLevelSystemCall.class);
	private PCMElementIDCache<ExternalCallAction> actionCache = new PCMElementIDCache<>(ExternalCallAction.class);

	public EvaluationAutomizer(PCMHeadlessClient client, EvaluationData data) {
		this.rest = client;
		this.data = data;
	}

	public void execute(int iterations, boolean respectAssemblys, HeadlessSimulationConfig config) {
		if (!rest.isReachable(2000L)) {
			throw new RuntimeException("Rest interface is not reachable!");
		}

		// load monitoring data
		MonitoringDataSet dataset = new KiekerMonitoringReader(data.getValidationFolder().getAbsolutePath());

		// load pcm data
		Repository repository = ModelUtil.readFromFile(data.getRepository().getAbsolutePath(), Repository.class);
		System system = ModelUtil.readFromFile(data.getSysten().getAbsolutePath(),
				org.palladiosimulator.pcm.system.System.class);
		ResourceEnvironment env = ModelUtil.readFromFile(data.getResourceenv().getAbsolutePath(),
				ResourceEnvironment.class);
		Allocation alloc = ModelUtil.readFromFile(data.getAllocation().getAbsolutePath(), Allocation.class);
		UsageModel usage = ModelUtil.readFromFile(data.getUsagemodel().getAbsolutePath(), UsageModel.class);

		// seff
		ResourceDemandingSEFF targetSeff = PCMUtil.getElementById(repository, ResourceDemandingSEFF.class,
				data.getTargetService());

		List<Double> ksTests = new ArrayList<>();
		List<Double> wassersteinDistances = new ArrayList<>();
		List<Double> avgsAnalysis = new ArrayList<>();
		List<Double> avgsMonitoring = new ArrayList<>();
		List<Double> devAnalysis = new ArrayList<>();
		List<Double> devMonitoring = new ArrayList<>();
		List<Pair<List<Double>, List<Double>>> distributions = new ArrayList<>();

		// parallel causes some problems
		ScheduledExecutorService execService = Executors.newScheduledThreadPool(1);

		for (int i = 0; i < iterations; i++) {
			final int k = i;
			Runnable iteration = () -> {
				java.lang.System.out.println("Starting iteration " + String.valueOf(k) + ".");

				rest.clear();

				SimulationClient client = rest.prepareSimulation();
				client.setAllocation(alloc);
				client.setRepository(repository);
				client.setResourceEnvironment(env);
				client.setSystem(system);
				client.setUsageModel(usage);

				client.setSimulationConfig(config);

				client.createTransitiveClosure();
				client.sync();

				InMemoryResultRepository results = simulateBlocking(client);

				// list with data
				List<Double> summedUp = new ArrayList<>();

				results.getValues().stream().forEach(sre -> {
					if (sre.getKey().getPoint().getType() == MeasuringPointType.ENTRY_LEVEL_CALL
							&& sre.getKey().getDesc().getId().equals(METRIC_RESPONSE_TIME)) {
						EntryLevelSystemCall systemCall = entryCallCache.resolve(usage,
								sre.getKey().getPoint().getSourceIds().get(0));

						if (systemCall.getOperationSignature__EntryLevelSystemCall().getId()
								.equals(targetSeff.getDescribedService__SEFF().getId())) {
							int c = 0;
							for (PlainDataSeries pds : sre.getValue()) {
								if (++c % 2 == 0) {
									for (PlainDataMeasure pdm : pds.getMeasures()) {
										double val = toDoubleValue(pdm.getV());
										if (!Double.isNaN(val)) {
											summedUp.add(val);
										}
									}
								}
							}
						}
					} else if (sre.getKey().getPoint().getType() == MeasuringPointType.ASSEMBLY_OPERATION
							&& sre.getKey().getDesc().getId().equals(METRIC_RESPONSE_TIME)) {
						ExternalCallAction action = actionCache.resolve(repository,
								sre.getKey().getPoint().getSourceIds().get(1));

						if (action.getCalledService_ExternalService().getId()
								.equals(targetSeff.getDescribedService__SEFF().getId())) {
							// only every second value
							int c = 0;
							for (PlainDataSeries pds : sre.getValue()) {
								if (++c % 2 == 0) {
									for (PlainDataMeasure pdm : pds.getMeasures()) {
										double val = toDoubleValue(pdm.getV());
										if (!Double.isNaN(val)) {
											summedUp.add(val);
										}
									}
								}
							}
						}
					}
				});

				// calculate the thing
				if (dataset.getServiceCalls().getServiceIds().contains(targetSeff.getId())) {
					double[] monitoring = dataset.getServiceCalls().getServiceCalls(targetSeff.getId()).stream()
							.map(call -> {
								return (call.getExitTime() - call.getEntryTime()) / 1000000D;
							}).mapToDouble(l -> l.doubleValue()).toArray();
					double[] analysis = summedUp.stream().mapToDouble(d -> d).toArray();

					if (monitoring.length > 0 && analysis.length > 0) {
						double wassersteinDistance = wasserstein(analysis, monitoring, false);

						wassersteinDistances.add(wassersteinDistance);
						ksTests.add(ksTestReal(analysis, monitoring));

						// avgs
						double avg1 = DoubleStream.of(monitoring).average().getAsDouble();
						double avg2 = DoubleStream.of(analysis).average().getAsDouble();

						// deviations
						StandardDeviation sd2 = new StandardDeviation(false);
						double dev1 = sd2.evaluate(monitoring);
						double dev2 = sd2.evaluate(analysis);

						avgsMonitoring.add(avg1);
						avgsAnalysis.add(avg2);
						devMonitoring.add(dev1);
						devAnalysis.add(dev2);

						List<Double> listA = Arrays.asList(ArrayUtils.toObject(analysis));
						List<Double> listB = Arrays.asList(ArrayUtils.toObject(monitoring));
						distributions.add(Pair.of(listA, listB));
					}
				}

				client.clear();
			};

			execService.submit(iteration);
		}

		// wait to finish
		execService.shutdown();
		try

		{
			execService.awaitTermination(2, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		java.lang.System.out.println(distributions.size());

		// output metrics
		java.lang.System.out.println("-------------------------------------");
		java.lang.System.out.println("Sample distribution comparison:");
		Pair<List<Double>, List<Double>> sampleDistributions = distributions
				.get(new Random().nextInt(distributions.size()));
		double[] analysisDistribution = cta(sampleDistributions.getLeft());
		double[] monitoringDistribution = cta(sampleDistributions.getRight());

		java.lang.System.out.println("[Min|Q1|Q2|Q3|MAX|AVG]");
		printDistributionOverview(monitoringDistribution);
		printDistributionOverview(analysisDistribution);

		java.lang.System.out.println("-------------------------------------");
		printMetricOverview("KS-Tests", ksTests);
		printMetricOverview("Wasserstein", wassersteinDistances);

		// save it to file
		if (data.getOutputJsonFile() != null) {
			ObjectMapper tempMapper = new ObjectMapper();
			EvaluationJsonData output = new EvaluationJsonData();
			distributions.forEach(distr -> {
				output.getDistributionsAnalysis().add(distr.getLeft());
				output.getDistributionsMonitoring().add(distr.getRight());
			});
			try {
				tempMapper.writeValue(data.getOutputJsonFile(), output);
			} catch (IOException e) {
				java.lang.System.out.println("Unable to write raw output.");
			}
		}
	}

	private void printMetricOverview(String name, List<Double> ksTests) {
		double[] obsv = cta(ksTests);
		double ksMin = minF.evaluate(obsv);
		double ksAvg = meanF.evaluate(obsv);
		double ksMax = maxF.evaluate(obsv);
		double q1 = this.q1.evaluate(obsv);
		double q3 = this.q3.evaluate(obsv);
		java.lang.System.out.println(name + ": (min: " + ksMin + ", avg: " + ksAvg + ", max: " + ksMax + ", Q1: " + q1
				+ ", Q3: " + q3 + ")");
	}

	private void printDistributionOverview(double[] distr) {
		double minA = minF.evaluate(distr);
		double q1A = q1.evaluate(distr);
		double avgA = meanF.evaluate(distr);
		double q2A = q2.evaluate(distr);
		double q3A = q3.evaluate(distr);
		double maxA = maxF.evaluate(distr);

		java.lang.System.out.println("[" + minA + "|" + q1A + "|" + q2A + "|" + q3A + "|" + maxA + "|" + avgA + "]");
	}

	private double[] cta(List<Double> list) {
		double[] res = new double[list.size()];
		int k = 0;
		for (double d : list) {
			res[k++] = d;
		}
		return res;
	}

	private InMemoryResultRepository simulateBlocking(SimulationClient client) {
		CountDownLatch latch = new CountDownLatch(1);
		final ResultHolder result = new ResultHolder();
		client.executeSimulation(res -> {
			result.result = res;
			latch.countDown();
		});

		try {
			latch.await();
			return result.result;
		} catch (InterruptedException e) {
			return null;
		}
	}

	private double toDoubleValue(AbstractMeasureValue v) {
		if (v instanceof DoubleMeasureValue) {
			return ((DoubleMeasureValue) v).getV();
		} else if (v instanceof LongMeasureValue) {
			return Long.valueOf(((LongMeasureValue) v).getV()).doubleValue();
		}
		return Double.NaN;
	}

	private double ksTestReal(double[] a, double[] b) {
		return new KolmogorovSmirnovTest().kolmogorovSmirnovStatistic(a, b);
	}

	private double wasserstein(double[] a, double[] b, boolean normed) {
		Arrays.sort(a);
		Arrays.sort(b);

		double minA = DoubleStream.of(a).min().getAsDouble();
		double minB = DoubleStream.of(b).min().getAsDouble();
		double maxA = DoubleStream.of(a).max().getAsDouble();
		double maxB = DoubleStream.of(b).max().getAsDouble();

		int minAB = (int) Math.floor(Math.min(minA, minB));
		int maxAB = (int) Math.ceil(Math.min(maxA, maxB));
		double[] transA = new double[maxAB - minAB];
		double[] transB = new double[maxAB - minAB];
		int currentPos = 0;

		while (currentPos < transA.length) {
			int currentLower = minAB + currentPos;
			int currentUpper = minAB + currentPos + 1;

			transA[currentPos] = (double) DoubleStream.of(a).filter(d -> d >= currentLower && d < currentUpper).count()
					/ (double) a.length;
			transB[currentPos++] = (double) DoubleStream.of(b).filter(d -> d >= currentLower && d < currentUpper)
					.count() / (double) b.length;
		}

		return new EarthMoversDistance().compute(transA, transB) / (normed ? (transA.length - 1) : 1d);
	}

	private static class ResultHolder {
		private InMemoryResultRepository result;
	}

}
