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

package com.github.houbie.lesscss.builder;


import com.github.houbie.lesscss.Options;
import com.github.houbie.lesscss.resourcereader.FileSystemResourceReader;
import com.github.houbie.lesscss.resourcereader.ResourceReader;
import com.github.houbie.lesscss.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A CompilationUnit contains all the details that a compiler needs to compile a Less file:
 * <ul>
 * <li>source</li>
 * <li>destination</li>
 * <li>compilation options</li>
 * </ul>
 *
 * @author Ivo Houbrechts
 */
public class CompilationUnit implements Serializable {
    private String sourceLocation;
    private File destination;
    private Options options;
    private ResourceReader resourceReader;
    private List<String> imports = new ArrayList<>();
    private String encoding;
    private long exceptionTimestamp;

    /**
     * Constructs a new CompilationUnit with default compilation options. Imports, if any, are searched in the directory containing the source.
     *
     * @param source      Less source file
     * @param destination CSS destination file
     */
    public CompilationUnit(File source, File destination) {
        this(source, destination, new Options());
    }

    /**
     * Constructs a new CompilationUnit with the given compilation options. Imports, if any, are searched in the directory containing the source.
     *
     * @param source      Less source file
     * @param destination CSS destination file
     * @param options     compilation options
     */
    public CompilationUnit(File source, File destination, Options options) {
        this(source.getName(), destination, options, new FileSystemResourceReader(source.getParentFile()));
    }

    /**
     * Constructs a new CompilationUnit with the given compilation options.
     * The source and the imports, if any, are resolved by the given ResourceReader.
     *
     * @param sourceLocation location of the LESS source in the file system or on the classpath
     * @param destination    CSS destination file
     * @param options        compilation options
     * @param resourceReader ResourceReader for resolving the source and the imports, if any.
     */
    public CompilationUnit(String sourceLocation, File destination, Options options, ResourceReader resourceReader) {
        if (StringUtils.isEmpty(sourceLocation) || destination == null || options == null) {
            throw new IllegalArgumentException("source, destination and options must be provided");
        }
        this.sourceLocation = sourceLocation;
        this.destination = destination.getAbsoluteFile();
        this.options = options;
        this.resourceReader = resourceReader;
    }

    public void setExceptionTimestamp(long timestamp) {
        this.exceptionTimestamp = timestamp;
    }


    /**
     * @return true if the source or one or more imported sources are older then the destination
     */
    public boolean isDirty() {
        if (!destination.exists() && exceptionTimestamp == 0) {
            return true;
        }
        long refTimeStamp = destination.exists() ? Math.max(destination.lastModified(), exceptionTimestamp) : exceptionTimestamp;
        if (resourceReader.lastModified(sourceLocation) > refTimeStamp) {
            return true;
        }
        if (imports != null) {
            for (String imported : imports) {
                if (resourceReader.lastModified(imported) > refTimeStamp) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public File getDestination() {
        return destination;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public ResourceReader getResourceReader() {
        return resourceReader;
    }

    public void setResourceReader(ResourceReader resourceReader) {
        this.resourceReader = resourceReader;
    }

    /**
     * Returns a list with (direct and indirect) imports.
     * The list is only complete after compilation or refresh from cache.
     *
     * @return the list of (direct and indirect) imports
     */
    public List<String> getImports() {
        return imports;
    }

    /**
     * @param imports the the list of (direct and indirect) imports
     */
    protected void setImports(List<String> imports) {
        this.imports = imports;
    }

    /**
     * Character encoding used for reading files and streams. Defaults to the java's default character encoding.
     *
     * @return character encoding
     */
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getSourceAsString() throws IOException {
        return resourceReader.read(sourceLocation);
    }

    /**
     * Checks whether two compilation units represent the same compilation.
     * Only the imports may be different when they have not yet been set.
     *
     * @param other CompilationUnit to compare with
     * @return true if the source, destination, encoding, options and resourceReader are the same.
     */
    public boolean isEquivalent(CompilationUnit other) {
        if (!destination.equals(other.destination)) return false;
        if (encoding != null ? !encoding.equals(other.encoding) : other.encoding != null) return false;
        if (resourceReader != null ? !resourceReader.equals(other.resourceReader) : other.resourceReader != null)
            return false;
        if (!options.equals(other.options)) return false;
        return sourceLocation.equals(other.sourceLocation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompilationUnit unit = (CompilationUnit) o;
        if (!isEquivalent(unit)) {
            return false;
        }
        return (imports == null) ? unit.imports == null : imports.equals(unit.imports);
    }

    @Override
    public int hashCode() {
        int result = sourceLocation.hashCode();
        result = 31 * result + destination.hashCode();
        result = 31 * result + options.hashCode();
        result = 31 * result + (resourceReader != null ? resourceReader.hashCode() : 0);
        result = 31 * result + (imports != null ? imports.hashCode() : 0);
        result = 31 * result + (encoding != null ? encoding.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompilationUnit{" +
                "sourceLocation=" + sourceLocation +
                ", destination=" + destination +
                '}';
    }
}
