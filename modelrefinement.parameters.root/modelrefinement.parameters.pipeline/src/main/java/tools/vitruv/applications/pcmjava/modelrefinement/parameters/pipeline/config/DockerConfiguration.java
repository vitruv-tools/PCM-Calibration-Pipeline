package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.config;

import java.io.Serializable;

public class DockerConfiguration implements Serializable {
	private String container;
	private String path;
	private String url;

	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
