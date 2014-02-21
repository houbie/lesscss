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

package com.github.houbie.lesscss.resourcereader

import spock.lang.Specification

class TrackingResourceReaderSpec extends Specification {

    def "test delegation"() {
        when:
        def delegate = new FileSystemResourceReader(new File('src/test/resources/ioutils'))
        def trackingResourceReader = new TrackingResourceReader(delegate)

        then:
        trackingResourceReader.canRead(location) == delegate.canRead(location)
        trackingResourceReader.lastModified(location) == delegate.lastModified(location)
        trackingResourceReader.read(location) == delegate.read(location)
        trackingResourceReader.readBytes(location) == delegate.readBytes(location)

        where:
        location << ['plain.txt', 'does not exist']
    }

    def "test read resources"() {
        when:
        def trackingResourceReader = new TrackingResourceReader(new ClasspathResourceReader())
        ['a', 'b', '../a', 'a/b/.././c'].each {
            trackingResourceReader.read(it)
        }

        then:
        trackingResourceReader.getReadResources() == ['a', 'b', '../a', 'a/c'] //read locations should be normalized
    }
}
