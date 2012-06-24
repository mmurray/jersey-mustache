package com.hazardousholdings.jersey.mustache.guice;

import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;
import javax.xml.ws.RequestWrapper;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.RequestScoped;
import com.hazardousholdings.jersey.mustache.MustacheViewProcessor;
import com.hazardousholdings.jersey.mustache.annotations.ClientsideTemplates;

public class MustacheModule extends AbstractModule {
	
	private final List<String> clientsideDirs = Lists.newArrayList();
	private String rootDir;
	private boolean liveCompilation = true;
	private Function<String, String> clientsidePreProcessor;
	
	protected void configureMustache() {}
	
	protected void setRootDirectory(String dir) {
		rootDir = dir;
	}
	
	protected void addClientsideTemplateDirectory(String dir) {
		clientsideDirs.add(dir);
	}
	
	protected void setClientsideTemplatePreprocessor(Function<String, String> preprocessor) {
		clientsidePreProcessor = preprocessor;
	}
	
	protected void setLiveCompilation(boolean live) {
		liveCompilation = live;
	}

	@Override
	protected void configure() {
		configureMustache();
		if (rootDir == null) {
			throw new RuntimeException("Must configure a template directory.");
		}
		bind(String.class)
			.annotatedWith(ClientsideTemplates.class)
			.toProvider(new ClientsideTemplatesProvider(rootDir, clientsideDirs, clientsidePreProcessor))
			.asEagerSingleton();
	}
	
	@Provides
	@Singleton
	MustacheViewProcessor provideViewProcessor(
			@Nullable ExecutorService executorService) {
		if (executorService != null) {
			return new MustacheViewProcessor(rootDir, liveCompilation, executorService);
		} else {
			return new MustacheViewProcessor(rootDir, liveCompilation);
		}
	}
	
	@Provides
	@RequestScoped
	Writer provideWriter(
			MustacheViewProcessor viewProcessor) throws Exception {
		Writer writer = viewProcessor.getWriter();
		if (writer == null) {
			throw new Exception("Attempted to inject the response writer before it was created.");
		}
		return viewProcessor.getWriter();
	}
}
