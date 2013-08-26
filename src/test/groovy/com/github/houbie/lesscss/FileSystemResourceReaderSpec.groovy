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

package com.github.houbie.lesscss

import com.github.houbie.lesscss.resourcereader.FileSystemResourceReader
import spock.lang.Specification

class FileSystemResourceReaderSpec extends Specification {

    def "read file from work dir"() {
        expect:
        new FileSystemResourceReader().read('src/test/resources/ioutils/plain.txt') == 'plain text'
    }

    def "read file from work dir with encoding"() {
        expect:
        new FileSystemResourceReader('utf16').read('src/test/resources/ioutils/utf16.txt') == 'utf16 text'
    }

    def "read file from specified dir"() {
        when:
        def reader = new FileSystemResourceReader(new File('src/test/resources'))
        then:
        reader.read('ioutils/plain.txt') == 'plain text'
        reader.read('../resources/ioutils/plain.txt') == 'plain text'
    }

    def "read absolute file"() {
        when:
        def reader = new FileSystemResourceReader(new File('src/test/resources'))
        then:
        reader.read(new File('src/test/resources/ioutils/plain.txt').absolutePath) == 'plain text'
    }

    def "read file from specified dir with encoding"() {
        when:
        def reader = new FileSystemResourceReader('utf16', new File('src/test/resources'))
        then:
        reader.read('ioutils/utf16.txt') == 'utf16 text'
        reader.read('../resources/ioutils/utf16.txt') == 'utf16 text'
    }

}
