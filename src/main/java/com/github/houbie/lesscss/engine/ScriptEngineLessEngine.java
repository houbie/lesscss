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
import com.github.houbie.lesscss.resourcereader.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.Reader;

public class ScriptEngineLessEngine implements LessEngine {
    private static Logger logger = LoggerFactory.getLogger(ScriptEngineLessEngine.class);

    ScriptEngine scriptEngine;

    public ScriptEngineLessEngine(String scriptEngineName) {
        logger.info("creating new NashornEngine");
        ScriptEngineManager factory = new ScriptEngineManager();
        scriptEngine = factory.getEngineByName(scriptEngineName);
        if (scriptEngine == null) {
            throw new RuntimeException("The ScriptEngine " + scriptEngineName + " could not be loaded");
        }
    }

    @Override
    public void initialize(Reader customJavaScriptReader) {
        try {
            if (customJavaScriptReader != null) {
                scriptEngine.eval(customJavaScriptReader);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String execute(String less, Options options, String sourceName, ResourceReader resourceReader) {
        Object result;
        Object parseException;
        try {
            result = ((Invocable) scriptEngine).invokeFunction("compile", less, options, sourceName, resourceReader);
            parseException = scriptEngine.get("parseException");
        } catch (Exception e) {
            throw new RuntimeException("Exception while compiling less", e);
        }
        if (parseException != null) {
            throw new LessParseException(parseException.toString());
        }
        return result.toString();
    }
}
