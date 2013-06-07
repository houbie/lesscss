package com.github.houbie.lesscss;

import com.github.houbie.lesscss.utils.IOUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class LessCompiler {
    public static final String NASHORN = "nashorn";
    public static final String RHINO = "rhino";

    private static final String ENVIRONMENT_SCRIPT = "js/environment.js";
    private static final String LESS_SCRIPT = "js/less-1.3.3.min.js";
    private static final String COMPILE_SCRIPT = "js/compile.js";
    private static final String UNKNOWN_SOURCE_NAME = "unknown";

    private static final Logger logger = Logger.getLogger(LessCompiler.class.getName());


    private ScriptEngine scriptEngine;
    private Reader customJavaScriptReader;
    private boolean prepared;


    public LessCompiler() {
        this((Reader) null);
    }

    public LessCompiler(String customJavaScript) {
        this(new StringReader(customJavaScript));
    }

    public LessCompiler(Reader customJavaScriptReader) {
        this(customJavaScriptReader, createScriptEngine());
    }

    protected LessCompiler(Reader customJavaScriptReader, ScriptEngine scriptEngine) {
        this.customJavaScriptReader = customJavaScriptReader;
        this.scriptEngine = scriptEngine;
    }

    public String compile(File source) throws IOException {
        return compile(source, new Options());
    }

    public String compile(File source, Options options) throws IOException {
        if (source == null) {
            throw new NullPointerException("less file may not be null");
        }
        return compile(IOUtils.read(source), new FileSystemResourceReader(source.getParentFile()), options, source.getName());
    }

    public List<String> compile(File source, File destination) throws IOException {
        return compile(source, destination, new Options(), new FileSystemResourceReader(source.getParentFile()), null);
    }

    public List<String> compile(File source, File destination, Options options, ResourceReader resourceReader, String encoding) throws IOException {
        if (source == null) {
            throw new NullPointerException("less file may not be null");
        }
        CompilationDetails details = compileWithDetails(IOUtils.read(source, encoding), resourceReader, options, source.getName());
        IOUtils.writeFile(details.getResult(), destination, encoding);
        return details.getImports();
    }

    public String compile(String less) {
        return compile(less, null, UNKNOWN_SOURCE_NAME);
    }

    public String compile(String less, ResourceReader resourceReader, String sourceName) {
        return compile(less, resourceReader, new Options(), sourceName);
    }

    public String compile(String less, ResourceReader resourceReader, Options options, String sourceName) {
        return compileWithDetails(less, resourceReader, options, sourceName).getResult();
    }

    public CompilationDetails compileWithDetails(String less, ResourceReader resourceReader, Options options, String sourceName) {
        if (less == null) {
            throw new NullPointerException("less string may not be null");
        }
        logger.fine("start less compilation");
        Object result;
        Object parseException;
        ImportReader importReader = new ImportReader(resourceReader);
        try {
            if (!prepared) {
                prepareScriptEngine();
            }
            bindVariables(importReader, options, sourceName);
            result = ((Invocable) scriptEngine).invokeFunction("_compile", less);
            parseException = scriptEngine.get("_parseException");
        } catch (Exception e) {
            throw new RuntimeException("Exception while compiling less", e);
        }
        if (parseException != null) {
            throw new RuntimeException(parseException.toString());
        }
        logger.fine("finished less compilation");
        return new CompilationDetails(result.toString(), importReader.getImports());
    }

    private void prepareScriptEngine() throws ScriptException, IOException {
        logger.info("prepareScriptEngine");
        Reader scriptReader = getLessScriptReader();
        try {
            scriptEngine.eval(scriptReader);
            if (customJavaScriptReader != null) {
                scriptEngine.eval(customJavaScriptReader);
            }
            prepared = true;
        } finally {
            if (scriptReader != null) {
                scriptReader.close();
            }
        }
    }

    private void bindVariables(ImportReader importReader, Options options, String sourceName) {
        scriptEngine.put("_importReader", importReader);
        scriptEngine.put("_compress", options.isCompress());
        scriptEngine.put("_optimizationLevel", options.getOptimizationLevel());
        scriptEngine.put("_strictImports", options.isStrictImports());
        scriptEngine.put("_rootPath", options.getRootPath());
        scriptEngine.put("_relativeUrls", options.isRelativeUrls());
        scriptEngine.put("_dumpLineNumbers", options.getDumpLineNumbers().getOptionString());
        scriptEngine.put("_sourceName", sourceName);
    }

    private Reader getLessScriptReader() {
        ClassLoader cl = getClass().getClassLoader();
        InputStream concatenatedScripts = new SequenceInputStream(cl.getResourceAsStream(ENVIRONMENT_SCRIPT), new SequenceInputStream(cl.getResourceAsStream(LESS_SCRIPT), cl.getResourceAsStream(COMPILE_SCRIPT)));
//        InputStream concatenatedScripts = cl.getResourceAsStream("js/less-1.3.3.js");
        return new InputStreamReader(concatenatedScripts);
    }

    private static ScriptEngine createScriptEngine() {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine scriptEngine = factory.getEngineByName(NASHORN);
        if (scriptEngine == null) {
            logger.info("nashorn Script Engine not available, using rhino");
            scriptEngine = factory.getEngineByName(RHINO);
        }
        if (scriptEngine == null) {
            throw new RuntimeException("No JavaScript script engine found");
        }
        return scriptEngine;
    }

    public static class ImportReader {
        private ResourceReader resourceReader;

        private List<String> imports = new ArrayList<>();

        public ImportReader(ResourceReader resourceReader) {
            this.resourceReader = resourceReader;
        }

        public String read(String location) throws IOException {
            logger.fine("reading @import " + location);
            if (resourceReader == null) {
                throw new RuntimeException("Error in less compilation: import of " + location + " failed because no ResourceReader is configured");
            }
            try {
                //resolve ./ and ../
                imports.add(new URI(location).normalize().getPath());
            } catch (URISyntaxException e) {
                logger.warning("exeption while normalizing import url: " + e.getMessage());
                imports.add(location);
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
