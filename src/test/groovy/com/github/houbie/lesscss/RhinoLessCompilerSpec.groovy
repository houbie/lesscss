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
import spock.lang.Unroll

import static com.github.houbie.lesscss.Options.LineNumbersOutput.*

class RhinoLessCompilerSpec extends AbstractLessCompilerSpec {

    def "compile files without specifying paths should not throw NPE"() {
        when:
        compiler.compile(new File('build.gradle'), new Options())

        then:
        thrown(LessParseException)

        when:
        compiler.compile(new File('build.gradle'), new File('import.css'))

        then:
        thrown(LessParseException)
    }

    def "compile file with imports"() {
        def file = new File('src/test/resources/less/import.less')
        def result = compiler.compileWithDetails(file.text, new FileSystemResourceReader(file.parentFile), new Options(), file.name)

        expect:
        result.result == new File('src/test/resources/less/import.css').text
        result.imports == ['import1/imported1.less', 'import1/import2/imported2.less', 'import1/commonImported.less', 'import1/import2/commonImported.less', 'imported0.less']
    }

    def "compile file with errors"() {
        when:
        compiler.compile(new File('src/test/resources/less/broken.less'))

        then:
        def e = thrown(Exception)
        e.message == 'less parse exception: missing closing `}`\n' +
                'in src/test/resources/less/broken.less at line 1\n' +
                'extract\n' +
                '#broken less {'
    }

    def "compile file with missing imports"() {
        when:
        compiler.compile(new File('src/test/resources/less/brokenImport.less'))

        then:
        def e = thrown(Exception)
        e.message == 'less parse exception: \'doesNotExist.less\' wasn\'t found\n' +
                'in src/test/resources/less/brokenImport.less at line 5\n' +
                'extract\n' +
                '@import "doesNotExist";'
    }

    def "less.js modify vars compatibility test"() {
        expect:
        compiler.compile(lessFile, new Options(modifyVars: getJson(lessFile))) == getCss(lessFile, 'modifyVars/').text

        where:
        lessFile << new File('src/test/resources/less.js-tests/less/modifyVars').listFiles().findAll {
            it.name.endsWith('.less')
        }
    }

    def "less.js source maps compatibility test"() {
        def lessFile = new File('src/test/resources/less.js-tests/less/sourcemaps/basic.less')
        expect:
        compiler.compileWithDetails(lessFile.text, new FileSystemResourceReader(lessFile.parentFile),
                new Options(modifyVars: getJson(lessFile), sourceMap: true, sourceMapRootpath: 'testweb/sourcemaps/'),
                lessFile.name, 'sourcemaps/basic.css', 'sourcemaps/basic.map').sourceMap == new File('src/test/resources/less.js-tests/sourcemaps/basic.json').text
    }

    @Unroll
    def "less.js debug compatibility test #dumpLineNumbers"() {
        def lessFile = new File('src/test/resources/less.js-tests/less/debug/linenumbers.less')

        expect:
        compiler.compile(lessFile, new Options(dumpLineNumbers: dumpLineNumbers)) == getCss(lessFile, 'debug/', '-' + dumpLineNumbers.optionString).text
                .replace('{pathimport}', 'import/').replace('{path}', 'src/test/resources/less.js-tests/less/debug/')
                .replace('{pathimportesc}', 'import\\/').replace('{pathesc}', '')

        where:
        dumpLineNumbers << [COMMENTS, MEDIA_QUERY, ALL]
    }

    @Unroll
    def "#lessFile.name compatibility test"() {
        expect:
        compiler.compile(lessFile.text, new FileSystemResourceReader(lessFile.parentFile, lessFile.parentFile.parentFile), new Options(strictMath: true, relativeUrls: true), lessFile.name) == getCss(lessFile).text

        where:
        lessFile << new File('src/test/resources/less.js-tests/less').listFiles().findAll { it.name.endsWith('.less') }
    }
}
