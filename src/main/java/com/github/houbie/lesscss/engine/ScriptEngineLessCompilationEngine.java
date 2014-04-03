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

package com.github.houbie.lesscss.engine;


import com.github.houbie.lesscss.LessParseException;
import com.github.houbie.lesscss.resourcereader.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.util.Map;

import static com.github.houbie.lesscss.LessCompiler.CompilationDetails;

/**
 * LessCompilationEngine implementation that uses a standard {@link javax.script.ScriptEngine} implementation.
 */
public class ScriptEngineLessCompilationEngine implements LessCompilationEngine {
    private static Logger logger = LoggerFactory.getLogger(ScriptEngineLessCompilationEngine.class);

    private static final String JS_ALL_MIN_JS = "js/all-min.js";
    private static final String LESS_SCRIPT = "js/less-rhino-1.7.0-mod.js";
    private static final String MINIFY_SCRIPT = "js/cssmin.js";
    private static final String COMPILE_SCRIPT = "js/compile.js";
    private static final boolean MINIFIED = true;


    private ScriptEngine scriptEngine;

    /**
     * @param scriptEngineName the name of the underlying ScriptEngine (e.g. "nashorn", "rhino", ...)
     */
    public ScriptEngineLessCompilationEngine(String scriptEngineName) {
        logger.info("creating new NashornEngine");
        ScriptEngineManager factory = new ScriptEngineManager();
        scriptEngine = factory.getEngineByName(scriptEngineName);
        if (scriptEngine == null) {
            throw new RuntimeException("The ScriptEngine " + scriptEngineName + " could not be loaded");
        }
    }

    /**
     * @param scriptEngine the underlying ScriptEngine
     */
    public ScriptEngineLessCompilationEngine(ScriptEngine scriptEngine) {
        logger.info("creating new engine with {}", scriptEngine.getClass());
        this.scriptEngine = scriptEngine;
    }

    @Override
    public void initialize(Reader customJavaScriptReader) {
        try {
            if (customJavaScriptReader != null) {
                scriptEngine.eval(customJavaScriptReader);
            }
            scriptEngine.eval(getLessScriptReader());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Reader getLessScriptReader() {
        ClassLoader cl = getClass().getClassLoader();
        InputStream concatenatedScripts;
        if (MINIFIED) {
            concatenatedScripts = cl.getResourceAsStream(JS_ALL_MIN_JS);
        } else {
            concatenatedScripts = new SequenceInputStream(cl.getResourceAsStream(LESS_SCRIPT), new SequenceInputStream(cl.getResourceAsStream(MINIFY_SCRIPT), cl.getResourceAsStream(COMPILE_SCRIPT)));
        }
        return new InputStreamReader(concatenatedScripts);
    }


    @Override
    public CompilationDetails compile(String less, CompilationOptions compilationOptions, ResourceReader resourceReader) {
        Map result;
        try {
            result = (Map) ((Invocable) scriptEngine).invokeFunction("compile", less, compilationOptions, resourceReader);
        } catch (Exception e) {
            throw new RuntimeException("Exception while compiling less", e);
        }
        if (result.get("parseException") != null) {
            throw new LessParseException((String) result.get("parseException"));
        }
        return new CompilationDetails((String) result.get("css"), (String) result.get("sourceMapContent"));
    }

    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }
}
