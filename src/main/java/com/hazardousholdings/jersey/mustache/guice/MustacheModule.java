package com.hazardousholdings.jersey.mustache.guice;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.hazardousholdings.jersey.mustache.MustacheViewProcessor;
import com.hazardousholdings.jersey.mustache.annotations.ClientsideTemplates;

public class MustacheModule extends AbstractModule {
	
	private final List<String> clientsideDirs = Lists.newArrayList();
	private String rootDir;
	
	protected void configureMustache() {}
	
	protected void setRootDirectory(String dir) {
		rootDir = dir;
	}
	
	protected void addClientsideTemplateDirectory(String dir) {
		clientsideDirs.add(dir);
	}

	@Override
	protected void configure() {
		configureMustache();
		if (rootDir == null) {
			throw new RuntimeException("Must configure a template directory.");
		}
		bind(MustacheViewProcessor.class).toInstance(new MustacheViewProcessor(rootDir));
		bind(String.class)
			.annotatedWith(ClientsideTemplates.class)
			.toProvider(new ClientsideTemplatesProvider(rootDir, clientsideDirs))
			.asEagerSingleton();
	}
}