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

class CombinedResourceReaderSpec extends Specification {
    def "test delegation"() {
        when:
        def delegate = new FileSystemResourceReader(new File('src/test/resources/ioutils'))
        def combiningResourceReader = new CombiningResourceReader(delegate)

        then:
        combiningResourceReader.canRead(location) == delegate.canRead(location)
        combiningResourceReader.lastModified(location) == delegate.lastModified(location)
        combiningResourceReader.read(location) == delegate.read(location)

        where:
        location << ['plain.txt', 'does not exist']
    }

    def "the first delegate wins if all delegates can read"() {
        when:
        def firstDelegate = new FileSystemResourceReader(new File('src/test/resources/less/import1'))
        def lastDelegate = new ClasspathResourceReader('less/import1/import2')
        def combiningResourceReader = new CombiningResourceReader(firstDelegate, lastDelegate)
        def location = 'commonImported.less'

        then:
        firstDelegate.read(location) == new File('src/test/resources/less/import1/commonImported.less').text
        lastDelegate.read(location) == new File('src/test/resources/less/import1/import2/commonImported.less').text
        combiningResourceReader.read(location) == firstDelegate.read(location)
    }

    def "the last delegate wins if the first does not resolve"() {
        when:
        def firstDelegate = new FileSystemResourceReader(new File('src/test/resources/less'))
        def lastDelegate = new ClasspathResourceReader('less/import1/import2')
        def combiningResourceReader = new CombiningResourceReader(firstDelegate, lastDelegate)
        def location = 'commonImported.less'

        then:
        !firstDelegate.canRead(location)
        lastDelegate.read(location) == new File('src/test/resources/less/import1/import2/commonImported.less').text
        combiningResourceReader.read(location) == lastDelegate.read(location)
    }
}
