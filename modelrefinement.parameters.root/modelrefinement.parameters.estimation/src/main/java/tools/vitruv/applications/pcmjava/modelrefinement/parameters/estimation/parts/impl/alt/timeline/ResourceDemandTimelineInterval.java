package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.timeline;

import java.util.List;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.TreeNode;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.records.ResponseTimeRecord;

public class ResourceDemandTimelineInterval {

	private TreeNode<AbstractResourceDemandTimelineObject> root;

	public ResourceDemandTimelineInterval(TreeNode<ServiceCall> callGraph, MonitoringDataSet data, String resourceId) {
		root = buildRecursive(callGraph, data, resourceId);
	}

	public TreeNode<AbstractResourceDemandTimelineObject> getRoot() {
		return root;
	}

	private TreeNode<AbstractResourceDemandTimelineObject> buildRecursive(TreeNode<ServiceCall> node,
			MonitoringDataSet data, String resourceId) {
		TreeNode<AbstractResourceDemandTimelineObject> root = buildNode(node.data);
		enrichNode(root, node.data, data, resourceId);

		for (TreeNode<ServiceCall> child : node.children) {
			root.children.add(buildRecursive(child, data, resourceId));
		}

		return root;
	}

	private void enrichNode(TreeNode<AbstractResourceDemandTimelineObject> root, ServiceCall data,
			MonitoringDataSet data2, String resourceId) {
		List<ResponseTimeRecord> records = data2.getResponseTimes().getResponseTimes(data.getServiceExecutionId());
		records.forEach(r -> {
			if (r.getResourceId().equals(resourceId)) {
				root.children.add(buildNode(r));
			}
		});
	}

	private TreeNode<AbstractResourceDemandTimelineObject> buildNode(ResponseTimeRecord r) {
		return new TreeNode<AbstractResourceDemandTimelineObject>(new InternalActionTimelineObject(r));
	}

	private TreeNode<AbstractResourceDemandTimelineObject> buildNode(ServiceCall data) {
		return new TreeNode<AbstractResourceDemandTimelineObject>(new ServiceCallTimelineObject(data));
	}

}
