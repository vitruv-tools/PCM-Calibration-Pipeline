package tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring;

import kieker.common.record.IMonitoringRecord;

public interface MonitoringRecordsWriter {

    void write(IMonitoringRecord monitoringRecord);

}