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

public class ImportCapturingResourceReader implements ResourceReader {
    private static final Logger logger = LoggerFactory.getLogger(LessCompilerImpl.class);

    private ResourceReader resourceReader;

    private List<String> imports = new ArrayList<>();

    public ImportCapturingResourceReader(ResourceReader resourceReader) {
        this.resourceReader = resourceReader;
    }

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

    public List<String> getImports() {
        return imports;
    }
}
