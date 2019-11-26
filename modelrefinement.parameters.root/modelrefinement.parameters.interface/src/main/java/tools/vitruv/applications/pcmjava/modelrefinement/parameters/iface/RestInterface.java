package tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.PipelineState;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.config.EPAPipelineConfiguration;

@RestController
public class RestInterface implements InitializingBean {
	private static final File configFile = new File("config.json");

	private PipelineState currentState;
	private RestPipeline pipeline;

	private ObjectMapper mapper = new ObjectMapper();

	@GetMapping("/state")
	public String getState() {
		return currentState.toString();
	}

	@GetMapping("/start")
	public String startPipeline() {
		if (pipeline != null) {
			pipeline.run();
		}
		return "";
	}

	@GetMapping("/results")
	public String getResults() {
		return prepareAnalysisResults();
	}

	@GetMapping("/conf")
	public String getConfiguration() {
		if (this.pipeline != null && this.pipeline.getConfig() != null) {
			try {
				return mapper.writeValueAsString(this.pipeline.getConfig());
			} catch (JsonProcessingException e) {
			}
		}
		return "null";
	}

	@GetMapping("/stats")
	public String getStats() {
		try {
			return mapper.writeValueAsString(pipeline.provideStatsJson());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "";
		}
	}

	@PostMapping("/create")
	public String createPipeline(@RequestBody String body) {
		try {
			EPAPipelineConfiguration config = mapper.readValue(URLDecoder.decode(body, "UTF-8"),
					EPAPipelineConfiguration.class);
			fillUpDefaults(config);
			saveToFile(config);
			this.pipeline = new RestPipeline(this, config);
			return "true";
		} catch (IOException e) {
			e.printStackTrace();
			return "false";
		}
	}

	private void saveToFile(EPAPipelineConfiguration config) {
		try {
			mapper.writeValue(configFile, config);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// DEFAULTS FOR THE DOCKER CONTAINER
	private void fillUpDefaults(EPAPipelineConfiguration config) {
		if (config.getDocker() == null) {
			config.setDockerImport(false);
		}

		if (config.getPcmBackendUrl() == null) {
			config.setPcmBackendUrl("http://127.0.0.1:8081/");
		}

		if (config.getJmxPath() == null) {
			config.setLoadTesting(false);
		} else {
			config.setLoadTesting(true);
		}

		if (config.getJmeterPath() == null) {
			config.setJmeterPath("/etc/jmeter/");
		}
	}

	public void setCurrentState(PipelineState state) {
		this.currentState = state;
	}

	public void setPipeline(RestPipeline pipeline) {
		this.pipeline = pipeline;
	}

	private String prepareAnalysisResults() {
		try {
			return mapper.writeValueAsString(this.pipeline.provideResultsJson());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (configFile.exists()) {
			EPAPipelineConfiguration config = mapper.readValue(configFile, EPAPipelineConfiguration.class);
			if (config != null) {
				fillUpDefaults(config);
				this.pipeline = new RestPipeline(this, config);
			}
		}
	}

}
