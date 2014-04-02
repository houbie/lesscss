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
import com.github.houbie.lesscss.compiledjs.LessImpl;
import com.github.houbie.lesscss.resourcereader.ResourceReader;
import com.github.houbie.mozilla.javascript.Context;
import com.github.houbie.mozilla.javascript.Function;
import com.github.houbie.mozilla.javascript.Scriptable;
import com.github.houbie.mozilla.javascript.tools.shell.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * LessCompilationEngine implementation that uses the Mozilla Rhino JavaScript engine to execute pre-compiled JavaScript.
 */
public class RhinoLessCompilationEngine implements LessCompilationEngine {
    private static Logger logger = LoggerFactory.getLogger(RhinoLessCompilationEngine.class);

    private boolean initialized;
    private Scriptable scope;
    private Function compileFunction;
    private String sourceMap;//TODO put in result

    @Override
    public void initialize(Reader customJavaScriptReader) {
        if (!initialized) {
            Context cx = Context.enter();
            logger.debug("Using implementation version: " + cx.getImplementationVersion());
            cx.setOptimizationLevel(9);
            cx.setLanguageVersion(170);
            Global global = new Global();
            global.init(cx);
            scope = cx.initStandardObjects(global);
            new LessImpl().exec(cx, scope);

            if (customJavaScriptReader != null) {
                try {
                    cx.evaluateReader(scope, customJavaScriptReader, "customJavaScript", 1, null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            compileFunction = (Function) scope.get("compile", scope);
            initialized = true;
        }
    }

    @Override
    public String compile(String less, CompilationOptions compilationOptions, ResourceReader resourceReader) {
        if (!initialized) {
            throw new RuntimeException("execute called, but not yet initialized");
        }

        Map result;
        Object parseException;
        try {
            Object[] args = {less, compilationOptions, resourceReader};

            result = (Map) Context.call(null, compileFunction, scope, scope, args);
            if (result.get("sourceMapContent") != null) {
                sourceMap = result.get("sourceMapContent").toString();
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception while compiling less", e);
        }
        if (result.get("parseException") != null) {
            throw new LessParseException(result.get("parseException").toString());
        }
        return result.get("css").toString();
    }

    @Override
    public String getSourceMap() {
        return sourceMap;
    }

}
