package tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring;

import java.util.ArrayList;
import java.util.List;

import kieker.common.record.IMonitoringRecord;
import kieker.monitoring.core.controller.IMonitoringController;

public class KiekerMonitoringRecordsWriter implements MonitoringRecordsWriter {

    private final IMonitoringController monitoringController;

    public KiekerMonitoringRecordsWriter(IMonitoringController monitoringController) {
        this.monitoringController = monitoringController;
    }

    @Override
    public synchronized void write(IMonitoringRecord monitoringRecord) {
        this.monitoringController.newMonitoringRecord(monitoringRecord);
    }
}
