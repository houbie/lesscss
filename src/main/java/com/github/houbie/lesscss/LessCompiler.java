package com.github.houbie.lesscss;

import com.github.houbie.lesscss.utils.IOUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class LessCompiler {
    protected static final String NASHORN = "nashorn";
    protected static final String RHINO = "rhino";

    private static final String ENVIRONMENT_SCRIPT = "META-INF/environment.js";
    private static final String LESS_SCRIPT = "META-INF/less-1.3.3.min.js";
    private static final String COMPILE_SCRIPT = "META-INF/compile.js";

    private static final Logger logger = Logger.getLogger(LessCompiler.class.getName());


    private ScriptEngine scriptEngine;
    private String customJavaScript;


    //TODO compile options: compress, optimization level...
    public LessCompiler() {
        createScriptEngine();
    }

    public LessCompiler(String customJavaScript) {
        createScriptEngine();
        this.customJavaScript = customJavaScript;
    }

    protected LessCompiler(ScriptEngine scriptEngine, String customJavaScript) {
        this.scriptEngine = scriptEngine;
        this.customJavaScript = customJavaScript;
    }

    public String compile(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("less file may not be null");
        }
        return compile(IOUtils.read(file), new FileSystemResourceReader(file.getParentFile()));
    }

    public String compile(String less) {
        return compile(less, null);
    }

    public String compile(String less, ResourceReader resourceReader) {
        return compileWithDetails(less, resourceReader).getResult();
    }

    public CompilationDetails compileWithDetails(String less, ResourceReader resourceReader) {
        if (less == null) {
            throw new NullPointerException("less string may not be null");
        }
        logger.fine("start less compilation");
        ImportReader importReader = new ImportReader(resourceReader);
        Object result = null;
        Object parseException = null;
        Reader scriptReader = null;
        try {
            try {
                scriptReader = getLessScriptReader();
                scriptEngine.eval(scriptReader);//TODO check performance difference with compiling script
                scriptEngine.put("importReader", importReader);
                result = ((Invocable) scriptEngine).invokeFunction("compile", less, true, 1);
                parseException = scriptEngine.get("parseException");
            } finally {
                if (scriptReader != null) {
                    scriptReader.close();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception while compiling less", e);
        }
        if (parseException != null) {
            throw new RuntimeException(parseException.toString());
        }
        logger.fine("finished less compilation");
        return new CompilationDetails(result.toString(), importReader.getImports());
    }

    private Reader getLessScriptReader() {
        ClassLoader cl = getClass().getClassLoader();
        InputStream concatenatedScripts = new SequenceInputStream(cl.getResourceAsStream(ENVIRONMENT_SCRIPT), new SequenceInputStream(cl.getResourceAsStream(LESS_SCRIPT), cl.getResourceAsStream(COMPILE_SCRIPT)));
        if (customJavaScript!=null){
            concatenatedScripts= new SequenceInputStream(concatenatedScripts, new ByteArrayInputStream(customJavaScript.getBytes()));
        }
        return new InputStreamReader(concatenatedScripts);
    }

    private void createScriptEngine() {
        ScriptEngineManager factory = new ScriptEngineManager();
        scriptEngine = factory.getEngineByName(NASHORN);
        if (scriptEngine == null) {
            logger.info("nashorn Script Engine not available, using rhino");
            scriptEngine = factory.getEngineByName(RHINO);
        }
        if (scriptEngine == null) {
            throw new RuntimeException("No JavaScript script engine found");
        }
    }

    public static class ImportReader {
        private ResourceReader resourceReader;

        private List<String> imports = new ArrayList<>();

        public ImportReader(ResourceReader resourceReader) {
            this.resourceReader = resourceReader;
        }

        public String read(String location) throws IOException {
            logger.fine("reading @import " + location);
            try {
                //resolve ./ and ../
                imports.add(new URI(location).normalize().getPath());
            } catch (URISyntaxException e) {
                imports.add(location);
            }
            if (resourceReader == null) {
                throw new RuntimeException("Error in less compilation: import of " + location + " failed because no ResourceReader is configured");
            }
            return resourceReader.read(location);
        }

        public List<String> getImports() {
            return imports;
        }
    }

    public static class CompilationDetails {
        private String result;
        private List<String> imports;

        public CompilationDetails(String result, List<String> imports) {
            this.result = result;
            this.imports = imports;
        }

        public String getResult() {
            return result;
        }

        public List<String> getImports() {
            return imports;
        }
    }
}
