package com.hazardousholdings.jersey.mustache;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    private final MustacheFactory mustacheFactory;
    private final Map<String, Mustache> compiledTemplates;
    private final String basePath;
    
    public MustacheViewProcessor() {
    	this("");
    }
    
    public MustacheViewProcessor(String path) {
        mustacheFactory = new DefaultMustacheFactory();
        compiledTemplates = new HashMap<String, Mustache>();
        basePath = path;

        precompileTemplates(new File(basePath));
    }
    
    private void precompileTemplates(File dir) {
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
    		Mustache m = mustacheFactory.compile(dir.getAbsolutePath());
    		compiledTemplates.put(key, m);
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
		compiledTemplates.get(resolvedPath).execute(new PrintWriter(out), viewable.getModel()).flush();
	}

}
