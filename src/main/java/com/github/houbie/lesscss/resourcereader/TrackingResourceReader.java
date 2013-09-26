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

import com.github.houbie.lesscss.LessCompilerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * ResourceReader implementation that keeps track of the resources that it reads.
 * Used to capture the import statements during a LESS compilation.
 * The actual reading of resources is delegated to another ResourceReader.
 *
 * @author Ivo Houbrechts
 */
public class TrackingResourceReader implements ResourceReader {
    private static final Logger logger = LoggerFactory.getLogger(LessCompilerImpl.class);

    private ResourceReader resourceReader;

    private List<String> imports = new ArrayList<>();

    /**
     * @param resourceReader the ResourceReader to delegate to
     */
    public TrackingResourceReader(ResourceReader resourceReader) {
        this.resourceReader = resourceReader;
    }

    @Override
    public String read(String location) throws IOException {
        logger.debug("reading @import " + location);
        if (resourceReader == null) {
            throw new RuntimeException("Error in less compilation: import of " + location + " failed because no ResourceReader is configured");
        }
        try {
            //resolve ./ and ../
            imports.add(new URI(location).normalize().getPath());
        } catch (URISyntaxException e) {
            logger.warn("exeption while normalizing import url: " + e.getMessage());
            imports.add(location);
        }
        return resourceReader.read(location);
    }

    @Override
    public long lastModified(String location) {
        return resourceReader.lastModified(location);
    }

    /**
     * @return the list of resources that have been read (imported)
     */
    public List<String> getImports() {
        return imports;
    }
}
