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

import java.io.IOException;

/**
 * ResourceReader implementation that searches multiple paths by delegating to a list of other ResourceReaders
 *
 * @author Ivo Houbrechts
 */
public class CombiningResourceReader implements ResourceReader {
    private ResourceReader[] resourceReaders;


    /**
     * @param resourceReaders the ResourceReaders to delegate to
     */
    public CombiningResourceReader(ResourceReader... resourceReaders) {
        if (resourceReaders.length == 0) {
            throw new IllegalArgumentException("at least one resourceReader has to be provided");
        }
        this.resourceReaders = resourceReaders;
    }

    @Override
    public boolean canRead(String location) throws IOException {
        for (ResourceReader resourceReader : resourceReaders) {
            if (resourceReader.canRead(location)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String read(String location) throws IOException {
        for (ResourceReader resourceReader : resourceReaders) {
            String result = resourceReader.read(location);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public byte[] readBytes(String location) throws IOException {
        for (ResourceReader resourceReader : resourceReaders) {
            byte[] result = resourceReader.readBytes(location);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public long lastModified(String location) {
        for (ResourceReader resourceReader : resourceReaders) {
            long result = resourceReader.lastModified(location);
            if (result != Long.MAX_VALUE) {
                return result;
            }
        }
        return Long.MAX_VALUE;
    }
}
