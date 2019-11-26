package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.test;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.config.EPAPipelineConfiguration;

public class ReadConfigurationTest {

	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {

		EPAPipelineConfiguration config = EPAPipelineConfiguration
				.fromFile(new File("casestudy-data/config/pipeline.config.json"));
		System.out.println(config.getPcmBackendUrl());

	}

}
