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

import com.github.houbie.lesscss.Options;

/**
 * Class that holds all the options to be passed to the javascript less parser.
 */
public class CompilationOptions {
    private Options options;
    private String sourceFilename;
    private String destinationFilename;
    private String sourceMapFilename;

    public CompilationOptions(Options options, String sourceFilename, String destinationFilename, String sourceMapFileName) {
        this.options = options;
        this.sourceFilename = sourceFilename;
        this.destinationFilename = destinationFilename;
        this.sourceMapFilename = sourceMapFileName;
    }

    public Options getOptions() {
        return options;
    }

    public String getSourceFilename() {
        return sourceFilename;
    }

    public String getDestinationFilename() {
        return destinationFilename;
    }

    public String getSourceMapFilename() {
        return sourceMapFilename;
    }
}
