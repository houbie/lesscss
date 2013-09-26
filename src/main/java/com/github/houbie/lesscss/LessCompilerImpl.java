/*
 * Copyright (c) 2013 Houbrechts IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.houbie.lesscss;

import com.github.houbie.lesscss.compiledjs.LessImpl;
import com.github.houbie.lesscss.resourcereader.FileSystemResourceReader;
import com.github.houbie.lesscss.resourcereader.ResourceReader;
import com.github.houbie.lesscss.resourcereader.TrackingResourceReader;
import com.github.houbie.lesscss.utils.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * LessCompiler implementation
 *
 * @author Ivo Houbrechts
 */
public class LessCompilerImpl implements LessCompiler {
    public static final String RHINO = "rhino";

    private static final String UNKNOWN_SOURCE_NAME = "unknown";

    private static final Logger logger = LoggerFactory.getLogger(LessCompilerImpl.class);


    private Reader customJavaScriptReader;
    private boolean prepared;
    private Scriptable scope;
    private Function compileFunction;


    /**
     * Default constructor
     */
    public LessCompilerImpl() {
        this((Reader) null);
    }

    /**
     * Constructor with custom JavaScript functions
     *
     * @param customJavaScript JavaScript functions that can be called in LESS sources
     */
    public LessCompilerImpl(String customJavaScript) {
        this(new StringReader(customJavaScript));
    }

    /**
     * Constructor with custom JavaScript functions
     *
     * @param customJavaScriptReader Reader for JavaScript functions that can be called in LESS sources
     */
    public LessCompilerImpl(Reader customJavaScriptReader) {
        this.customJavaScriptReader = customJavaScriptReader;
    }

    @Override
    public String compile(File source) throws IOException {
        return compile(source, new Options());
    }

    @Override
    public String compile(File source, Options options) throws IOException {
        if (source == null) {
            throw new NullPointerException("less file may not be null");
        }
        return compile(IOUtils.read(source), new FileSystemResourceReader(source.getParentFile()), options, source.getName());
    }

    @Override
    public void compile(File source, File destination) throws IOException {
        compile(source, destination, new Options(), new FileSystemResourceReader(source.getParentFile()), null);
    }

    @Override
    public void compile(File source, File destination, Options options, ResourceReader importReader, String encoding) throws IOException {
        if (source == null) {
            throw new NullPointerException("less file may not be null");
        }
        CompilationDetails details = compileWithDetails(IOUtils.read(source, encoding), importReader, options, source.getName());
        IOUtils.writeFile(details.getResult(), destination, encoding);
    }

    @Override
    public String compile(String less) {
        return compile(less, null, UNKNOWN_SOURCE_NAME);
    }

    @Override
    public String compile(String less, ResourceReader importReader, String sourceName) {
        return compile(less, importReader, new Options(), sourceName);
    }

    @Override
    public String compile(String less, ResourceReader importReader, Options options, String sourceName) {
        return compileWithDetails(less, importReader, options, sourceName).getResult();
    }

    @Override
    public CompilationDetails compileWithDetails(String less, ResourceReader importReader, Options options, String sourceName) {
        if (less == null) {
            throw new NullPointerException("less string may not be null");
        }
        logger.debug("start less compilation");
        Object result;
        Object parseException;
        TrackingResourceReader trackingResourceReader = new TrackingResourceReader(importReader);
        try {
            if (!prepared) {
                prepareScriptEngine();
            }
            Object[] args = {less, options, sourceName, trackingResourceReader};
            result = Context.call(null, compileFunction, scope, scope, args);
            parseException = scope.get("parseException", scope);
        } catch (Exception e) {
            throw new RuntimeException("Exception while compiling less", e);
        }
        if (parseException != null) {
            throw new RuntimeException(parseException.toString());
        }
        logger.debug("finished less compilation");
        return new CompilationDetails(result.toString(), trackingResourceReader.getImports());
    }

    private void prepareScriptEngine() throws IOException {
        logger.info("prepareScriptEngine");

        Context cx = Context.enter();
        logger.debug("Using implementation version: " + cx.getImplementationVersion());
        cx.setOptimizationLevel(9);
        cx.setLanguageVersion(170);
        Global global = new Global();
        global.init(cx);
        scope = cx.initStandardObjects(global);
        new LessImpl().exec(cx, scope);

        if (customJavaScriptReader != null) {
            cx.evaluateReader(scope, customJavaScriptReader, "customJavaScript", 1, null);
        }
        compileFunction = (Function) scope.get("compile", scope);
        prepared = true;
    }

}
