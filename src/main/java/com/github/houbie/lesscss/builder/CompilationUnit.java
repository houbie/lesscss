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
import com.github.houbie.lesscss.utils.IOUtils;

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
    private static final long serialVersionUID = 3784487076551298600L;

    private File source;
    private File destination;
    private Options options;
    private ResourceReader importReader;
    private List<String> imports = new ArrayList<>();
    private String encoding;
    private long exceptionTimestamp;

    /**
     * Constructs a new CompilationUnit with default compilation options. Imports are searched in the dir of the source.
     *
     * @param source      Less source file
     * @param destination CSS destination file
     */
    public CompilationUnit(File source, File destination) {
        this(source, destination, new Options());
    }

    /**
     * Constructs a new CompilationUnit with the given compilation options. Imports are searched in the dir of the source.
     *
     * @param source      Less source file
     * @param destination CSS destination file
     * @param options     compilation options
     */
    public CompilationUnit(File source, File destination, Options options) {
        this(source, destination, options, new FileSystemResourceReader(source.getParentFile()));
    }

    /**
     * Constructs a new CompilationUnit with the given compilation options and import reader.
     *
     * @param source       Less source file
     * @param destination  CSS destination file
     * @param options      compilation options
     * @param importReader ResourceReader for resolving imports
     */
    public CompilationUnit(File source, File destination, Options options, ResourceReader importReader) {
        if (source == null || destination == null || options == null) {
            throw new IllegalArgumentException("source, destination and options may not be null");
        }
        //Convert the files to absolute paths to prevent that equals returns true for files with the same name in a different location.
        this.source = source.getAbsoluteFile();
        this.destination = destination.getAbsoluteFile();
        this.options = options;
        this.importReader = importReader;
    }

    public void setExceptionTimestamp(long timestamp) {
        this.exceptionTimestamp = timestamp;
    }


    /**
     * @return true if this if the source or one or more imported sources are older then the destination
     */
    public boolean isDirty() {
        if (!destination.exists() && exceptionTimestamp == 0) {
            return true;
        }
        long refTimeStamp = destination.exists() ? Math.max(destination.lastModified(), exceptionTimestamp) : exceptionTimestamp;
        if (source.lastModified() > refTimeStamp) {
            return true;
        }
        if (imports != null) {
            for (String imported : imports) {
                if (importReader.lastModified(imported) > refTimeStamp) {
                    return true;
                }
            }
        }
        return false;
    }

    public File getSource() {
        return source;
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

    public ResourceReader getImportReader() {
        return importReader;
    }

    public void setImportReader(ResourceReader importReader) {
        this.importReader = importReader;
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
        if (!source.exists()) {
            throw new RuntimeException("Source does not exists: " + source);
        }
        return IOUtils.read(source, encoding);
    }

    /**
     * Checks whether two compilation units represent the same compilation.
     * Only the imports may be different when they have not yet been set.
     *
     * @param other CompilationUnit to compare with
     * @return true if the source, destination, encoding, options and importReader are the same.
     */
    public boolean isEquivalent(CompilationUnit other) {
        if (!destination.equals(other.destination)) return false;
        if (encoding != null ? !encoding.equals(other.encoding) : other.encoding != null) return false;
        if (importReader != null ? !importReader.equals(other.importReader) : other.importReader != null) return false;
        if (!options.equals(other.options)) return false;
        return source.equals(other.source);
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
        int result = source.hashCode();
        result = 31 * result + destination.hashCode();
        result = 31 * result + options.hashCode();
        result = 31 * result + (importReader != null ? importReader.hashCode() : 0);
        result = 31 * result + (imports != null ? imports.hashCode() : 0);
        result = 31 * result + (encoding != null ? encoding.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompilationUnit{" +
                "source=" + source +
                ", destination=" + destination +
                '}';
    }
}
