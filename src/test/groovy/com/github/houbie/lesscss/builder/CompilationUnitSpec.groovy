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

package com.github.houbie.lesscss.builder

import com.github.houbie.lesscss.resourcereader.ResourceReader
import spock.lang.Specification

class CompilationUnitSpec extends Specification {
    File source = new File('src/test/resources/less/import.less')
    File destination = new File('src/test/resources/less/import.css')


    def 'isDirty when destination does not exist'() {
        def unit = new CompilationUnit(source, new File('doesNotExist'))

        expect:
        unit.isDirty()
    }

    def 'isDirty when destination does not exist but LessParseException newer than source'() {
        def unit = new CompilationUnit(source, new File('doesNotExist'))
        unit.exceptionTimestamp = source.lastModified() + 1;

        expect:
        !unit.isDirty()
    }

    def 'isDirty when destination older than source'() {
        def unit = new CompilationUnit(createNewFile(), destination)
        unit.exceptionTimestamp = source.lastModified() + 1;

        expect:
        unit.isDirty()
    }

    def 'isDirty when destination is older than import'() {
        ResourceReader importReader = Mock()
        File newDestination = createNewFile()
        def unit = new CompilationUnit(source, newDestination)
        unit.importReader = importReader
        unit.imports = ['import1', 'import2']

        when:
        def dirty = unit.isDirty()

        then:
        dirty
        1 * importReader.lastModified('import1') >> 0
        1 * importReader.lastModified('import2') >> Long.MAX_VALUE
    }

    def 'not isDirty when destination is newer than imports and source'() {
        ResourceReader importReader = Mock()
        File newDestination = createNewFile()
        def unit = new CompilationUnit(source, newDestination)
        unit.importReader = importReader
        unit.imports = ['import1', 'import2']

        when:
        def dirty = unit.isDirty()

        then:
        !dirty
        2 * importReader.lastModified(_) >>> [0, 0]
    }

    private File createNewFile() {
        File newFile = new File("build/tmp/${System.currentTimeMillis()}")
        newFile.parentFile.mkdirs()
        newFile.createNewFile()
        return newFile
    }
}
