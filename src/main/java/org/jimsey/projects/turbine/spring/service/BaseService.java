package org.jimsey.projects.turbine.spring.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

//@PropertySource("classpath:conf/${environment}/turbine.properties")
public class BaseService {
	
	@Autowired
	protected Environment environment;

	@Value("${default.property}")
	protected String defaultProperty;

	@Value("${what.environment}")
	protected String whatEnvironment;

	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@PostConstruct
	public void init() {
		logger.info(String.format("what.environment=%s", whatEnvironment));
		logger.info(String.format("defaultProperty=%s", defaultProperty));
		logger.info("BaseService init completed");
	}

}
