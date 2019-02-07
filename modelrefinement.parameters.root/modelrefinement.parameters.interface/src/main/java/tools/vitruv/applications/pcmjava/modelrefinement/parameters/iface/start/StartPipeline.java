package tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.start;

import org.springframework.boot.SpringApplication;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.RestApplication;

public class StartPipeline {

	public static void main(String[] args) {
		SpringApplication.run(RestApplication.class, args);
	}

}
