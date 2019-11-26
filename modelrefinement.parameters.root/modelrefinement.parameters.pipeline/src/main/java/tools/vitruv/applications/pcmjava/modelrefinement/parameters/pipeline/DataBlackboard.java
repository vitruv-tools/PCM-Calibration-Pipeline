package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.pcm.headless.shared.data.results.InMemoryResultRepository;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.data.ServiceResults;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.data.StatResults;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.InMemoryPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.LocalFilesystemPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.util.PrepareResultsUtil;

public class DataBlackboard {
	private MonitoringDataSet monitoringData;

	private InMemoryPCM loadedPcm;
	private LocalFilesystemPCM filesystemPcm;

	private List<InMemoryResultRepository> analysisResults;
	private List<ServiceResults> serviceResults;

	private List<List<StatResults>> statResults;

	private PipelineState state;

	private List<IPipelineStateListener> listeners;

	public DataBlackboard() {
		this.listeners = new ArrayList<>();
		this.analysisResults = new ArrayList<>();
		this.serviceResults = new ArrayList<>();
		this.statResults = new ArrayList<>();
	}

	public MonitoringDataSet getMonitoringData() {
		return monitoringData;
	}

	public synchronized void setMonitoringData(MonitoringDataSet monitoringData) {
		this.monitoringData = monitoringData;
	}

	public InMemoryPCM getLoadedPcm() {
		return loadedPcm;
	}

	public synchronized void setLoadedPcm(InMemoryPCM loadedPcm) {
		this.loadedPcm = loadedPcm;
	}

	public LocalFilesystemPCM getFilesystemPcm() {
		return filesystemPcm;
	}

	public synchronized void setFilesystemPcm(LocalFilesystemPCM filesystemPcm) {
		this.filesystemPcm = filesystemPcm;
	}

	public synchronized void persistInMemoryPCM() {
		getLoadedPcm().saveToFilesystem(getFilesystemPcm());
	}

	public List<InMemoryResultRepository> getAnalysisResults() {
		return analysisResults;
	}

	public synchronized void addAnalysisResults(InMemoryResultRepository rawAnalysisResults) {
		this.analysisResults.add(rawAnalysisResults);

		// derive stats and service results
		this.statResults.add(PrepareResultsUtil.prepareStatsResults(rawAnalysisResults, this));
		this.serviceResults.add(PrepareResultsUtil.prepareServiceResults(rawAnalysisResults, this));
	}

	public PipelineState getState() {
		return state;
	}

	public void setState(PipelineState state) {
		this.state = state;
		this.listeners.forEach(s -> s.onChange(state));
	}

	public List<IPipelineStateListener> getListeners() {
		return listeners;
	}

	public List<List<StatResults>> getStatResults() {
		return statResults;
	}

	public List<ServiceResults> getServiceResults() {
		return serviceResults;
	}

}
