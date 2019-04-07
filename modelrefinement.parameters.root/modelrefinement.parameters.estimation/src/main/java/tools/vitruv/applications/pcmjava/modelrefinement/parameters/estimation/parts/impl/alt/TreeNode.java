package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.alt;

import java.util.LinkedList;
import java.util.List;

public class TreeNode<T> {

	public T data;
	public TreeNode<T> parent;
	public List<TreeNode<T>> children;

	public TreeNode(T data) {
		this.data = data;
		this.children = new LinkedList<TreeNode<T>>();
	}

	public TreeNode<T> addChild(T child) {
		TreeNode<T> childNode = new TreeNode<T>(child);
		childNode.parent = this;
		this.children.add(childNode);
		return childNode;
	}

	// other features ...

}