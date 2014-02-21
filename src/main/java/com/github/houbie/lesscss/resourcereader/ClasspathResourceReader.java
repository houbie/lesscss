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
import com.github.houbie.lesscss.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * ResourceReader implementation that searches the classpath relative to a base path.
 *
 * @author Ivo Houbrechts
 */
public class ClasspathResourceReader implements ResourceReader {
    private static Logger logger = LoggerFactory.getLogger(ClasspathResourceReader.class);

    private String basePath;
    private String encoding;

    /**
     * Construct a new ClasspathResourceReader using java's default character encoding
     */
    public ClasspathResourceReader() {
        this(null);
    }

    /**
     * Construct a new ClasspathResourceReader using java's default character encoding
     *
     * @param basePath the relative path to resolve resources within the classpath
     */
    public ClasspathResourceReader(String basePath) {
        this(basePath, null);
    }

    /**
     * Construct a new ClasspathResourceReader
     *
     * @param basePath the relative path to resolve resources within the classpath
     * @param encoding the encoding used for reading files
     */
    public ClasspathResourceReader(String basePath, String encoding) {
        setBasePath(basePath);
        this.encoding = encoding;
    }

    private void setBasePath(String basePath) {
        if (StringUtils.isEmpty(basePath)) {
            this.basePath = "";
        } else {
            this.basePath = basePath.trim();
            if (this.basePath.startsWith("/")) {
                this.basePath = this.basePath.substring(1);
            }
            if (!this.basePath.endsWith("/")) {
                this.basePath += '/';
            }
        }
    }

    @Override
    public boolean canRead(String location) {
        return getUrl(location) != null;
    }

    @Override
    public String read(String location) throws IOException {
        if (StringUtils.isEmpty(location)) {
            return null;
        }
        logger.debug("reading {} with basePath {}", location, basePath);
        URL url = getUrl(location);
        return (url != null) ? IOUtils.read(url, encoding) : null;
    }

    @Override
    public byte[] readBytes(String location) throws IOException {
        if (StringUtils.isEmpty(location)) {
            return null;
        }
        logger.debug("reading bytes {} with basePath {}", location, basePath);
        URL url = getUrl(location);
        if (url != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copyLarge(url.openStream(), bos);
            return bos.toByteArray();
        }
        return null;
    }

    private URL getUrl(String location) {
        String path = location.startsWith("/") ? location.substring(1) : basePath + location;
        return getClass().getClassLoader().getResource(path);
    }

    @Override
    public long lastModified(String location) {
        return canRead(location) ? Long.MIN_VALUE : Long.MAX_VALUE;
    }

    /**
     * @return the relative path to resolve resources within the classpath
     */
    public String getBasePath() {
        return basePath;
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

        ClasspathResourceReader that = (ClasspathResourceReader) o;

        if (!basePath.equals(that.basePath)) return false;
        if (encoding != null ? !encoding.equals(that.encoding) : that.encoding != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = basePath.hashCode();
        result = 31 * result + (encoding != null ? encoding.hashCode() : 0);
        return result;
    }
}
