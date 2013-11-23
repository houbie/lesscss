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
import com.github.houbie.lesscss.Options;
import com.github.houbie.lesscss.compiledjs.LessImpl;
import com.github.houbie.lesscss.resourcereader.ResourceReader;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;

public class RhinoLessEngine implements LessEngine {
    private static Logger logger = LoggerFactory.getLogger(RhinoLessEngine.class);

    private boolean initialized;
    private Scriptable scope;
    private Function compileFunction;

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
    public String execute(String less, Options options, String sourceName, ResourceReader resourceReader) {
        if (!initialized) {
            throw new RuntimeException("execute called, but not yet initialized");
        }

        Object result;
        Object parseException;
        try {
            Object[] args = {less, options, sourceName, resourceReader};

            result = Context.call(null, compileFunction, scope, scope, args);
            parseException = scope.get("parseException", scope);
        } catch (Exception e) {
            throw new RuntimeException("Exception while compiling less", e);
        }
        if (parseException != null) {
            throw new LessParseException(parseException.toString());
        }
        return result.toString();
    }

}
