package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.data.usage;

import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.mapping.MonitoringDataMapping;

public abstract class AbstractUsageElement {

	public abstract void merge(AbstractUsageElement other);

	public abstract AbstractUserAction toUserAction(System sys, Repository repo, MonitoringDataMapping mapping);

	public abstract boolean matches(AbstractUsageElement b);

}
