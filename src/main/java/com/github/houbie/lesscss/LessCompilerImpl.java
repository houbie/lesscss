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

import com.github.houbie.lesscss.engine.CompilationOptions;
import com.github.houbie.lesscss.engine.LessCompilationEngine;
import com.github.houbie.lesscss.resourcereader.FileSystemResourceReader;
import com.github.houbie.lesscss.resourcereader.ResourceReader;
import com.github.houbie.lesscss.resourcereader.TrackingResourceReader;
import com.github.houbie.lesscss.utils.IOUtils;
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
    private static final String UNKNOWN_SOURCE_NAME = "unknown";

    private static final Logger logger = LoggerFactory.getLogger(LessCompilerImpl.class);

    private Reader customJavaScriptReader;
    private LessCompilationEngine engine;

    /**
     * Default constructor
     *
     * @param engine javascript LessEngine
     */
    public LessCompilerImpl(LessCompilationEngine engine) {
        this(engine, (Reader) null);
    }

    /**
     * Constructor with custom JavaScript functions
     *
     * @param engine           javascript LessEngine
     * @param customJavaScript JavaScript functions that can be called in LESS sources
     */
    public LessCompilerImpl(LessCompilationEngine engine, String customJavaScript) {
        this(engine, new StringReader(customJavaScript));
    }

    /**
     * Constructor with custom JavaScript functions
     *
     * @param engine                 javascript LessEngine
     * @param customJavaScriptReader Reader for JavaScript functions that can be called in LESS sources
     */
    public LessCompilerImpl(LessCompilationEngine engine, Reader customJavaScriptReader) {
        this.engine = engine;
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
        return compile(IOUtils.read(source), new FileSystemResourceReader(source.getAbsoluteFile().getParentFile()), options, source.getPath());
    }

    @Override
    public void compile(File source, File destination) throws IOException {
        compile(source, destination, new Options(), new FileSystemResourceReader(source.getAbsoluteFile().getParentFile()), null);
    }

    @Override
    public void compile(File source, File destination, Options options, ResourceReader importReader, String encoding) throws IOException {
        if (source == null) {
            throw new NullPointerException("less file may not be null");
        }
        CompilationDetails details = compileWithDetails(IOUtils.read(source, encoding), importReader, options, source.getPath(), destination.getPath(), getSourceMapFileName(source.getPath()));
        IOUtils.writeFile(details.getResult(), destination, encoding);
    }

    @Override
    public String compile(String less) {
        return compile(less, null, UNKNOWN_SOURCE_NAME);
    }

    @Override
    public String compile(String less, ResourceReader importReader, String sourceFilename) {
        return compile(less, importReader, new Options(), sourceFilename);
    }

    @Override
    public String compile(String less, ResourceReader importReader, Options options, String sourceFilename) {
        return compileWithDetails(less, importReader, options, sourceFilename, getDestinationFileName(sourceFilename), getSourceMapFileName(sourceFilename)).getResult();
    }

    @Override
    public String compile(String less, ResourceReader importReader, Options options, String sourceFilename, String destinationFilename, String sourceMapFilename) {
        return compileWithDetails(less, importReader, options, sourceFilename, destinationFilename, sourceMapFilename).getResult();
    }

    @Override
    public CompilationDetails compileWithDetails(String less, ResourceReader importReader, Options options, String sourceFilename) {
        return compileWithDetails(less, importReader, options, sourceFilename, getDestinationFileName(sourceFilename), getSourceMapFileName(sourceFilename));
    }

    @Override
    public CompilationDetails compileWithDetails(String less, ResourceReader importReader, Options options, String sourceFilename, String destinationFilename, String sourceMapFilename) {
        if (less == null) {
            throw new NullPointerException("less string may not be null");
        }
        logger.debug("start less compilation");
        TrackingResourceReader trackingResourceReader = new TrackingResourceReader(importReader);
        engine.initialize(customJavaScriptReader);
        CompilationOptions compilationOptions = new CompilationOptions(options, sourceFilename, destinationFilename, sourceMapFilename);
        String result = engine.compile(less, compilationOptions, trackingResourceReader);

        logger.debug("finished less compilation");
        return new CompilationDetails(result, engine.getSourceMap(), trackingResourceReader.getReadResources());
    }

    private String getSourceMapFileName(String sourceFilename) {
        return sourceFilename + ".map";
    }

    private String getDestinationFileName(String sourceFilename) {
        String destinationFilename = sourceFilename != null ? sourceFilename : UNKNOWN_SOURCE_NAME;
        return destinationFilename.replace(".less", ".css");
    }

}
