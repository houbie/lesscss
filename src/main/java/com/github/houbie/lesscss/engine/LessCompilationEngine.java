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

import com.github.houbie.lesscss.resourcereader.ResourceReader;

import java.io.Reader;

import static com.github.houbie.lesscss.LessCompiler.CompilationDetails;

/**
 * A JavaScript engine that compiles LESS to CSS
 */
public interface LessCompilationEngine {
    /**
     * Initialize the engine.
     *
     * @param customJavaScriptReader JavaScript functions that can be called in LESS sources. May be null.
     */
    void initialize(Reader customJavaScriptReader);


    /**
     * Compile a LESS String
     *
     * @param less               LESS source
     * @param compilationOptions compilation options
     * @param resourceReader     ResourceReader for resolving imports
     * @return the compiled CSS en source map in a CompilationDetails instance
     */
    CompilationDetails compile(String less, CompilationOptions compilationOptions, ResourceReader resourceReader);
}
