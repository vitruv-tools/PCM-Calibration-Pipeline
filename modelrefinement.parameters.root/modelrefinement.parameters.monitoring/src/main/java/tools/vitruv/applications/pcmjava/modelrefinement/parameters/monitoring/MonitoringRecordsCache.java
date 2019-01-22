package tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring;

import java.util.ArrayList;
import java.util.List;

import kieker.common.record.IMonitoringRecord;
import kieker.monitoring.core.controller.IMonitoringController;

public class MonitoringRecordsCache implements MonitoringRecordsWriter {

    private final List<IMonitoringRecord> cacheEntries;

    private final MonitoringRecordsWriter recordWriter;

    public MonitoringRecordsCache(int initialCacheSize, MonitoringRecordsWriter recordWriter) {
        this.recordWriter = recordWriter;
        this.cacheEntries = new ArrayList<IMonitoringRecord>(initialCacheSize);
    }

    public int getItemsCount() {
        return this.cacheEntries.size();
    }

    public synchronized void flushCache() {
        for (IMonitoringRecord iMonitoringRecord : this.cacheEntries) {
            this.recordWriter.write(iMonitoringRecord);
        }
        this.cacheEntries.clear();
    }

    @Override
    public synchronized void write(IMonitoringRecord monitoringRecord) {
        this.cacheEntries.add(monitoringRecord);
    }
}
