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

package com.github.houbie.lesscss.resourcereader;


import com.github.houbie.lesscss.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * ResourceReader implementation that reads files relative to one or more base directories.
 *
 * @author Ivo Houbrechts
 */
public class FileSystemResourceReader implements ResourceReader {
    private static Logger logger = LoggerFactory.getLogger(FileSystemResourceReader.class);

    private File[] baseDirs;
    private String encoding;

    /**
     * Construct a new FileSystemResourceReader for the current directory and java's default character encoding
     */
    public FileSystemResourceReader() {
        this(new File("."));
    }

    /**
     * Construct a new FileSystemResourceReader for the current directory
     *
     * @param encoding the encoding used for reading files
     */
    public FileSystemResourceReader(String encoding) {
        this(encoding, new File("."));
    }

    /**
     * Construct a new FileSystemResourceReader using java's default character encoding
     *
     * @param baseDirs the directories that are used to resolve resources
     */
    public FileSystemResourceReader(File... baseDirs) {
        this(null, baseDirs);
    }

    /**
     * Construct a new FileSystemResourceReader
     *
     * @param encoding the encoding used for reading files
     * @param baseDirs the directories that are used to resolve resources
     */
    public FileSystemResourceReader(String encoding, File... baseDirs) {
        copyBaseDirs(baseDirs);
        this.encoding = encoding;
    }

    /**
     * Convert the dirs to absolute paths to prevent that equals returns true for directories with the same name in a different location.
     *
     * @param baseDirs the directories that are used to resolve resources
     */
    private void copyBaseDirs(File[] baseDirs) {
        this.baseDirs = new File[baseDirs.length];
        for (int i = 0; i < baseDirs.length; i++) {
            this.baseDirs[i] = baseDirs[i].getAbsoluteFile();
        }
    }

    @Override
    public boolean canRead(String location) {
        File file = resolve(location);
        return file != null && file.exists();
    }

    @Override
    public String read(String location) throws IOException {
        File file = resolve(location);
        if (file != null) {
            logger.debug("reading {}", location);
            return IOUtils.read(file, encoding);
        }
        return null;
    }

    private File resolve(String location) {
        File file = resolveRelative(location);
        if (file == null) {
            file = resolveAbsolute(location);
        }
        return file;
    }

    private File resolveRelative(String location) {
        for (File dir : baseDirs) {
            logger.debug("resolving {} relative to {}", location, dir.getAbsolutePath());
            File file = new File(dir, location);
            if (file.canRead()) {
                return file;
            }
        }
        return null;
    }

    private File resolveAbsolute(String location) {
        File file = new File(location);
        return (file.canRead()) ? file : null;
    }

    @Override
    public long lastModified(String location) {
        File file = resolve(location);
        return (file == null) ? Long.MAX_VALUE : file.lastModified();
    }

    /**
     * @return the directories that are used to resolve resources
     */
    public File[] getBaseDirs() {
        return baseDirs;
    }

    /**
     * @return the encoding used for reading files
     */
    public String getEncoding() {
        return encoding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileSystemResourceReader that = (FileSystemResourceReader) o;

        if (!Arrays.equals(baseDirs, that.baseDirs)) return false;
        if (encoding != null ? !encoding.equals(that.encoding) : that.encoding != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = baseDirs != null ? Arrays.hashCode(baseDirs) : 0;
        result = 31 * result + (encoding != null ? encoding.hashCode() : 0);
        return result;
    }
}
