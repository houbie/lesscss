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
import java.io.Serializable;

/**
 * An object that resolves and reads resources
 *
 * @author Ivo Houbrechts
 */
public interface ResourceReader extends Serializable {
    /**
     * Returns true if the resource at the given location can be read.
     *
     * @param location the location of the resource
     * @return true if the resource can be read
     * @throws IOException
     */
    boolean canRead(String location) throws IOException;

    /**
     * Read a resource into a String
     *
     * @param location the location of the resource
     * @return the content of the resource, or null if the resource cannot be resolved
     * @throws IOException
     */
    String read(String location) throws IOException;

    /**
     * @param location the location of the resource
     * @return timestamp of last modification of the resource, or Long.MAX_VALUE if the resource cannot be resolved
     */
    long lastModified(String location);
}
