package tools.vitruv.applications.pcmjava.modelrefinement.parameters;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.impl.BranchDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.impl.LoopDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.impl.ResourceUtilizationDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.impl.ResponseTimeDataSet;

/**
 * This interface summarizes the monitoring data sets needed for the
 * {@link SeffParameterEstimation}. The records of the loop, branch and response
 * time reverence records of the service call data set.
 * 
 * @author JP
 *
 */
public interface MonitoringDataSet {

	/**
	 * Gets the branches monitoring data.
	 * 
	 * @return branches monitoring data set.
	 */
	BranchDataSet getBranches();

	/**
	 * Gets the loops monitoring data.
	 * 
	 * @return loops monitoring data set.
	 */
	LoopDataSet getLoops();

	/**
	 * Gets the resource utilization monitoring data.
	 * 
	 * @return resource utilization monitoring data set.
	 */
	ResourceUtilizationDataSet getResourceUtilizations();

	/**
	 * Gets the response time monitoring data.
	 * 
	 * @return response time monitoring data set.
	 */
	ResponseTimeDataSet getResponseTimes();

	/**
	 * Gets the service call monitoring data. Records of this data set may be
	 * referenced by other monitoring data, like loop records.
	 * 
	 * @return service call monitoring data set.
	 */
	ServiceCallDataSet getServiceCalls();

}