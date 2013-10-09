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
     * Read a resource into a String
     *
     * @param location the location of the resource
     * @return the content of the resource
     * @throws IOException
     */
    String read(String location) throws IOException;

    /**
     * @param location the location of the resource
     * @return timestamp of last modification of the resource
     */
    long lastModified(String location);
}