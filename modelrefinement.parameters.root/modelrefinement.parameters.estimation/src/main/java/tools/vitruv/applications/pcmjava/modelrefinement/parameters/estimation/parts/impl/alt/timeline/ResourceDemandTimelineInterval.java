package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.timeline;

import java.util.List;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt.TreeNode;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.records.ResponseTimeRecord;

public class ResourceDemandTimelineInterval {

	private TreeNode<AbstractTimelineObject> root;

	public ResourceDemandTimelineInterval(TreeNode<ServiceCall> callGraph, MonitoringDataSet data, String resourceId) {
		root = buildRecursive(callGraph, null, data, resourceId);
	}

	public TreeNode<AbstractTimelineObject> getRoot() {
		return root;
	}

	private TreeNode<AbstractTimelineObject> buildRecursive(TreeNode<ServiceCall> node,
			TreeNode<AbstractTimelineObject> parent, MonitoringDataSet data, String resourceId) {

		TreeNode<AbstractTimelineObject> root = buildNode(node.data);
		root.parent = parent;
		enrichNode(root, node.data, data, resourceId);

		for (TreeNode<ServiceCall> child : node.children) {
			root.children.add(buildRecursive(child, root, data, resourceId));
		}

		return root;
	}

	private void enrichNode(TreeNode<AbstractTimelineObject> root, ServiceCall data, MonitoringDataSet data2,
			String resourceId) {
		List<ResponseTimeRecord> records = data2.getResponseTimes().getResponseTimes(data.getServiceExecutionId());
		records.forEach(r -> {
			if (r.getResourceId().equals(resourceId)) {
				TreeNode<AbstractTimelineObject> temp = buildNode(r);
				temp.parent = root;
				root.children.add(temp);
			}
		});
	}

	private TreeNode<AbstractTimelineObject> buildNode(ResponseTimeRecord r) {
		return new TreeNode<AbstractTimelineObject>(new InternalActionTimelineObject(r));
	}

	private TreeNode<AbstractTimelineObject> buildNode(ServiceCall data) {
		return new TreeNode<AbstractTimelineObject>(new ServiceCallTimelineObject(data));
	}

}
