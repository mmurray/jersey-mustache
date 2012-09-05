package com.hazardousholdings.jersey.mustache;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.template.ViewProcessor;

@Provider
public class MustacheViewProcessor implements ViewProcessor<String> {
	
    public final static String MUSTACHE_TEMPLATES_BASE_PATH =
            "com.hazardousholdings.jersey.mustache.templateBasePath";
    
    private DefaultMustacheFactory mustacheFactory;
    private Writer writer;
    private final Map<String, Mustache> compiledTemplates;
    private final String basePath;
    private final boolean live;
    private final ExecutorService executorService;
    
    public MustacheViewProcessor() {
    	this("", true);
    }
    
    public MustacheViewProcessor(String path, boolean live) {
    	this(path, live, Executors.newCachedThreadPool());
    }
    
    public MustacheViewProcessor(
    		String path,
    		boolean live,
    		@Nullable ExecutorService executorService) {
        compiledTemplates = new HashMap<String, Mustache>();
        basePath = path;
        this.live = live;
        this.executorService = executorService;
        precompileTemplates(new File(basePath));
    }
   

    public Writer getWriter() {
    	return writer;
    }
    
    private void precompileTemplates(File dir) {
    	if (dir == null) {
    		return;
    	}
    	for (File f : dir.listFiles()) {
    		precompileTemplatesRecursively(f, "");
    	}
    }
    
    private void precompileTemplatesRecursively(File dir, String namespace) {
		namespace += '/';
    	
    	if (dir.isDirectory()) {
    		namespace += dir.getName();
    		for (File f : dir.listFiles()) {
    			precompileTemplatesRecursively(f, namespace);
    		}
    	} else if (dir.exists()) {
    		String key = namespace + dir.getName();
    		mustacheFactory = new DefaultMustacheFactory(new File(basePath));
    		if (executorService != null) {
    			mustacheFactory.setExecutorService(executorService);
    		}
    		Mustache m = mustacheFactory.compile(key);
    		compiledTemplates.put(key, m);
    		System.out.println(key);
    	}
    }

	public String resolve(String path) {
		if (compiledTemplates.containsKey(path)) {
			return path;
		}
		return null;
	}

	public void writeTo(String resolvedPath, Viewable viewable, OutputStream out)
			throws IOException {
		if (live) {
			precompileTemplates(new File(basePath));
		}
		writer = new PrintWriter(out);
		compiledTemplates.get(resolvedPath).execute(writer, viewable.getModel()).flush();
	}

}
