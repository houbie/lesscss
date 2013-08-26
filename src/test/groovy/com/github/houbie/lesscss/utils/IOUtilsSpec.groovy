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

package com.github.houbie.lesscss.utils

import spock.lang.Specification

class IOUtilsSpec extends Specification {

    def "read a file without encoding"() {
        expect:
        IOUtils.read(new File('src/test/resources/ioutils/plain.txt')) == 'plain text'
    }

    def "read a file with encoding"() {
        expect:
        IOUtils.read(new File('src/test/resources/ioutils/utf16.txt'), 'utf16') == 'utf16 text'
    }

    def "read a url without encoding"() {
        expect:
        IOUtils.read(IOUtils.classLoader.getResource('ioutils/plain.txt')) == 'plain text'
    }

    def "read a url with encoding"() {
        expect:
        IOUtils.read(IOUtils.classLoader.getResource('ioutils/utf16.txt'), 'utf16') == 'utf16 text'
    }

    def "read from reader"() {
        def s = 'read from reader'

        expect:
        IOUtils.read(new StringReader(s)) == s
    }

    def "write file with default encoding"() {
        def file = new File('build/tmp/ioutilstest.txt')
        IOUtils.writeFile('content', file, null)

        expect:
        file.text == 'content'
    }

    def "write file UTF16"() {
        def file = new File('build/tmp/ioutilstest.txt')
        IOUtils.writeFile('content', file, 'UTF16')

        expect:
        file.getText('UTF16') == 'content'
    }
}
