package tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.start;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.RestApplication;

public class StartPipeline {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(RestApplication.class);
		app.setDefaultProperties(Collections.singletonMap("server.port", "8081"));
		ConfigurableApplicationContext ctx = app.run(args);
	}

}
