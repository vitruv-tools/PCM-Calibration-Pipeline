package tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.PipelineState;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.config.EPAPipelineConfiguration;

@RestController
public class RestInterface {

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

	@PostMapping("/create")
	public String createPipeline(@RequestBody String body) {
		try {
			EPAPipelineConfiguration config = mapper.readValue(body, EPAPipelineConfiguration.class);
			this.pipeline = new RestPipeline(this, config);
			return "true";
		} catch (IOException e) {
			return "false";
		}
	}

	public void setCurrentState(PipelineState state) {
		this.currentState = state;
	}

	public void setPipeline(RestPipeline pipeline) {
		this.pipeline = pipeline;
	}

}
