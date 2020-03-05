package tools.vitruv.applications.pcmjava.modelrefinement.parameters.impl;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.OutputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.annotation.Property;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import kieker.common.record.system.CPUUtilizationRecord;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.records.ResourceUtilizationRecord;

/**
 * Kieker monitoring records filter, which filters {@link CPUUtilizationRecord}
 * and transforms them into {@link ResourceUtilizationRecord}.
 * 
 * @author JP
 *
 */
@Plugin(description = "Filter, transforming CPUUtilizationRecord into ResourceUtilizationRecord.", outputPorts = {
		@OutputPort(name = KiekerCpuUtilizationConverterFilter.OUTPUT_PORT_NAME_EVENTS, description = "Outputs ResourceUtilizationRecords", eventTypes = {
				ResourceUtilizationRecord.class }) }, configuration = {
						@Property(name = KiekerCpuUtilizationConverterFilter.CONFIG_PROPERTY_NAME_SESSION_ID, defaultValue = ""),
						@Property(name = KiekerCpuUtilizationConverterFilter.CONFIG_PROPERTY_NAME_RESOURCE_ID, defaultValue = "") })
public class KiekerCpuUtilizationConverterFilter extends AbstractFilterPlugin {

	/**
	 * The name of the input port for incoming events.
	 */
	public static final String INPUT_PORT_NAME_EVENTS = "inputEvent";

	/**
	 * The name of the output port for outgoing events.
	 */
	public static final String OUTPUT_PORT_NAME_EVENTS = "outputEvent";

	/**
	 * The name of the configuration property which sets the session id.
	 */
	public static final String CONFIG_PROPERTY_NAME_SESSION_ID = "SessionId";

	/**
	 * The name of the configuration property which sets the resource id.
	 */
	public static final String CONFIG_PROPERTY_NAME_RESOURCE_ID = "ResourceId";

	private final String sessionId;

	private final String resourceId;

	/**
	 * Initializes a new instance of {@link KiekerCpuUtilizationConverterFilter}.
	 * Each Plugin requires a constructor with a Configuration object and a
	 * IProjectContext.
	 * 
	 * @param configuration  The configuration for this component.
	 * @param projectContext The project context for this component. The component
	 *                       will be registered.
	 */
	public KiekerCpuUtilizationConverterFilter(final Configuration configuration,
			final IProjectContext projectContext) {
		super(configuration, projectContext);
		this.sessionId = configuration.getStringProperty(CONFIG_PROPERTY_NAME_SESSION_ID);
		this.resourceId = configuration.getStringProperty(CONFIG_PROPERTY_NAME_RESOURCE_ID);
	}

	@Override
	public Configuration getCurrentConfiguration() {
		Configuration config = new Configuration();
		config.putIfAbsent(CONFIG_PROPERTY_NAME_SESSION_ID, this.sessionId);
		config.putIfAbsent(CONFIG_PROPERTY_NAME_RESOURCE_ID, this.resourceId);
		return config;
	}

	/**
	 * This method is called by kieker for each record of the type specified by
	 * {@link InputPort}.
	 * 
	 * @param record The record of the specified type.
	 */
	@InputPort(name = INPUT_PORT_NAME_EVENTS, description = "Input for records.", eventTypes = {
			CPUUtilizationRecord.class })
	public final void inputEvent(final CPUUtilizationRecord record) {
		if (record.getCpuID().equals("0") == false) {
			return;
		}
		ResourceUtilizationRecord transformedRecord = new ResourceUtilizationRecord(this.sessionId,
				record.getHostname(), this.resourceId, record.getTotalUtilization(), record.getTimestamp());

		super.deliver(OUTPUT_PORT_NAME_EVENTS, transformedRecord);
	}
}
