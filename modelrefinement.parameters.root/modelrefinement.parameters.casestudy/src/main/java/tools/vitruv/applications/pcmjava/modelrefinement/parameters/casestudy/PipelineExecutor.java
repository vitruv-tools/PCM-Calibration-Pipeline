package tools.vitruv.applications.pcmjava.modelrefinement.parameters.casestudy;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.RestApplication;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.RestInterface;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.RestPipeline;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.config.EPAPipelineConfiguration;

/**
 * Uses the CoCoME case study to evaluate the pipeline and the whole concept.
 * 
 * @author David Monschein
 *
 */
public class PipelineExecutor {

	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		EPAPipelineConfiguration config = EPAPipelineConfiguration
				.fromFile(new File("casestudy-data/config/pipeline.config.json"));

		SpringApplication app = new SpringApplication(RestApplication.class);
		app.setDefaultProperties(Collections.singletonMap("server.port", "80"));
		ConfigurableApplicationContext ctx = app.run(args);
		RestInterface iface = ctx.getBean(RestInterface.class);
		iface.setPipeline(new RestPipeline(iface, config));

	}

}
