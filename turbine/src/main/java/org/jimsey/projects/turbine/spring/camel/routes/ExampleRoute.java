package org.jimsey.projects.turbine.spring.camel.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ExampleRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		System.out.println(String.format("%s configured in camel context %s", this.getClass().getName(), getContext().getName()));

	}

}