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

import com.github.houbie.lesscss.resourcereader.ResourceReader;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * LESS compiler interface
 *
 * @author Ivo Houbrechts
 */
public interface LessCompiler {
    /**
     * Compile a LESS file
     *
     * @param source source file
     * @return the resulting CSS
     * @throws IOException When source cannot be read
     */
    String compile(File source) throws IOException;

    /**
     * Compile a LESS file
     *
     * @param source  source file
     * @param options compilation options
     * @return the resulting CSS
     * @throws IOException When source cannot be read
     */
    String compile(File source, Options options) throws IOException;

    /**
     * Compile a LESS file
     *
     * @param source      source file
     * @param destination destination file
     * @throws IOException When source cannot be read or destination cannot be written
     */
    void compile(File source, File destination) throws IOException;

    /**
     * Compile a LESS file
     *
     * @param source       source file
     * @param destination  destination file
     * @param options      compilation options
     * @param importReader ResourceReader for resolving imports
     * @param encoding     character encoding
     * @throws IOException When source cannot be read or destination cannot be written
     */
    void compile(File source, File destination, Options options, ResourceReader importReader, String encoding) throws IOException;

    /**
     * Compile a LESS String
     *
     * @param less LESS source
     * @return the resulting CSS
     */
    String compile(String less);

    /**
     * Compile a LESS String
     *
     * @param less              LESS source
     * @param importReader      ResourceReader for resolving imports
     * @param sourceFilename name of the LESS source that can be used for reporting errors
     * @return the resulting CSS
     */
    String compile(String less, ResourceReader importReader, String sourceFilename);

    /**
     * Compile a LESS String
     *
     * @param less           LESS source
     * @param importReader   ResourceReader for resolving imports
     * @param options        compilation options
     * @param sourceFilename name of the LESS source that can be used for reporting errors
     * @return the resulting CSS
     */
    String compile(String less, ResourceReader importReader, Options options, String sourceFilename);

    /**
     * Compile a LESS String
     *
     * @param less                LESS source
     * @param importReader        ResourceReader for resolving imports
     * @param options             compilation options
     * @param sourceFilename      name of the LESS source that can be used for reporting errors and source map generation
     * @param destinationFilename name of the output that can be used for source map generation
     * @param sourceMapFilename   the source map file name
     * @return the resulting CSS
     */
    String compile(String less, ResourceReader importReader, Options options, String sourceFilename, String destinationFilename, String sourceMapFilename);

    /**
     * Compile a LESS String
     *
     * @param less           LESS source
     * @param importReader   ResourceReader for resolving imports
     * @param options        compilation options
     * @param sourceFilename name of the LESS source that can be used for reporting errors and source map generation
     * @return CompilationDetails that holds both the resulting CSS and the list of (recursive) imports
     */
    CompilationDetails compileWithDetails(String less, ResourceReader importReader, Options options, String sourceFilename);

    /**
     * Compile a LESS String
     *
     * @param less                LESS source
     * @param importReader        ResourceReader for resolving imports
     * @param options             compilation options
     * @param sourceFilename      name of the LESS source that can be used for reporting errors and source map generation
     * @param destinationFilename name of the output that can be used for source map generation
     * @param sourceMapFilename   the source map file name
     * @return CompilationDetails that holds both the resulting CSS, the source map and the list of (recursive) imports
     */
    CompilationDetails compileWithDetails(String less, ResourceReader importReader, Options options, String sourceFilename, String destinationFilename, String sourceMapFilename);

    public static class CompilationDetails {
        private String result;
        private String sourceMap;
        private List<String> imports;

        public CompilationDetails(String result, String sourceMap) {
            this.result = result;
            this.sourceMap = sourceMap;
        }

        public String getResult() {
            return result;
        }

        public String getSourceMap() {
            return sourceMap;
        }

        public List<String> getImports() {
            return imports;
        }

        public void setImports(List<String> imports) {
            this.imports = imports;
        }
    }
}
