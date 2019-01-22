package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline;

import java.util.ArrayList;
import java.util.List;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.results.PalladioAnalysisResults;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.InMemoryPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.LocalFilesystemPCM;

public class DataBlackboard {
	private MonitoringDataSet monitoringData;

	private InMemoryPCM loadedPcm;
	private LocalFilesystemPCM filesystemPcm;

	private PalladioAnalysisResults analysisResults;

	private PipelineState state;

	private List<IPipelineStateListener> listeners;

	public DataBlackboard() {
		this.listeners = new ArrayList<>();
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

	public PalladioAnalysisResults getAnalysisResults() {
		return analysisResults;
	}

	public synchronized void setAnalysisResults(PalladioAnalysisResults analysisResults) {
		this.analysisResults = analysisResults;
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

}
