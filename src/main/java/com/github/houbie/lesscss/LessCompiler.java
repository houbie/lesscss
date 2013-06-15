package com.github.houbie.lesscss;

import com.github.houbie.lesscss.utils.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class LessCompiler {
    public static final String RHINO = "rhino";

    private static final String UNKNOWN_SOURCE_NAME = "unknown";

    private static final Logger logger = LoggerFactory.getLogger(LessCompiler.class);


    private Reader customJavaScriptReader;
    private boolean prepared;
    private Scriptable scope;
    private Function compileFunction;


    public LessCompiler() {
        this((Reader) null);
    }

    public LessCompiler(String customJavaScript) {
        this(new StringReader(customJavaScript));
    }

    public LessCompiler(Reader customJavaScriptReader) {
        this.customJavaScriptReader = customJavaScriptReader;
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
        logger.debug("start less compilation");
        Object result;
        Object parseException;
        ImportReader importReader = new ImportReader(resourceReader);
        try {
            if (!prepared) {
                prepareScriptEngine();
            }
            Object[] args = {less, options, sourceName, importReader};
            result = Context.call(null, compileFunction, scope, scope, args);
            parseException = scope.get("parseException", scope);
        } catch (Exception e) {
            throw new RuntimeException("Exception while compiling less", e);
        }
        if (parseException != null) {
            throw new RuntimeException(parseException.toString());
        }
        logger.debug("finished less compilation");
        return new CompilationDetails(result.toString(), importReader.getImports());
    }

    private void prepareScriptEngine() throws IOException {
        logger.info("prepareScriptEngine");
        Reader scriptReader = getLessScriptReader();

        try {
            Context cx = Context.enter();
            logger.debug("Using implementation version: " + cx.getImplementationVersion());
            cx.setOptimizationLevel(9);
            cx.setLanguageVersion(170);
            Global global = new Global();
            global.init(cx);
            scope = cx.initStandardObjects(global);
            cx.evaluateReader(scope, scriptReader, "environment+less-1.3.3+compileFunction.js", 1, null);

            if (customJavaScriptReader != null) {
                cx.evaluateReader(scope, customJavaScriptReader, "customJavaScript", 1, null);
            }
            compileFunction = (Function) scope.get("compile", scope);
            prepared = true;
        } finally {
            if (scriptReader != null) {
                scriptReader.close();
            }
        }
    }

    private Reader getLessScriptReader() {
        ClassLoader cl = getClass().getClassLoader();
        return new InputStreamReader(cl.getResourceAsStream("js/all-min.js"));
    }

    public static class ImportReader {
        private ResourceReader resourceReader;

        private List<String> imports = new ArrayList<>();

        public ImportReader(ResourceReader resourceReader) {
            this.resourceReader = resourceReader;
        }

        public String read(String location) throws IOException {
            logger.debug("reading @import " + location);
            if (resourceReader == null) {
                throw new RuntimeException("Error in less compilation: import of " + location + " failed because no ResourceReader is configured");
            }
            try {
                //resolve ./ and ../
                imports.add(new URI(location).normalize().getPath());
            } catch (URISyntaxException e) {
                logger.warn("exeption while normalizing import url: " + e.getMessage());
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
