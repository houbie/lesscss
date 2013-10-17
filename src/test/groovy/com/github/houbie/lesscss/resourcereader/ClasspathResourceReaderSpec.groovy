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

class ClasspathResourceReaderSpec extends Specification {

    def "read when location is a full path"() {
        expect:
        new ClasspathResourceReader().read('ioutils/plain.txt') == 'plain text'
    }

    def "read with base path"() {
        when:
        def reader = new ClasspathResourceReader('ioutils')

        then:
        reader.read('plain.txt') == 'plain text'
        reader.read('../ioutils/plain.txt') == 'plain text'
    }

    def "read absolute location"() {
        when:
        def reader = new ClasspathResourceReader('ioutils')

        then:
        reader.read('/ioutils/plain.txt') == 'plain text'
    }

    def "read with base path and encoding"() {
        when:
        def reader = new ClasspathResourceReader('ioutils', 'utf16')

        then:
        reader.read('utf16.txt') == 'utf16 text'
        reader.read('../ioutils/utf16.txt') == 'utf16 text'
        reader.read('/ioutils/utf16.txt') == 'utf16 text'
    }

    def "test canRead"() {
        when:
        def reader = new ClasspathResourceReader('ioutils')

        then:
        reader.canRead(location) == canRead

        where:
        location               | canRead
        'plain.txt'            | true
        '/ioutils/plain.txt'   | true
        '../ioutils/plain.txt' | true
        'cannot read'          | false
    }

    def "test lastModified"() {
        when:
        def reader = new ClasspathResourceReader('ioutils')

        then:
        reader.lastModified(location) == lastModified

        where:
        location      | lastModified
        'plain.txt'   | Long.MIN_VALUE
        'cannot read' | Long.MAX_VALUE
    }
}
