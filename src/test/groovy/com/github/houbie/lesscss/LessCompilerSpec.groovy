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

import com.github.houbie.lesscss.engine.RhinoLessCompilationEngine
import com.github.houbie.lesscss.resourcereader.FileSystemResourceReader
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.Unroll

import static com.github.houbie.lesscss.Options.LineNumbersOutput.*

class LessCompilerSpec extends Specification {

    static LessCompilerImpl compiler

    def setupSpec() {
        Reader reader = new File('src/test/resources/less.js-tests/functions.js').newReader()
        compiler = new LessCompilerImpl(new RhinoLessCompilationEngine(), reader)
    }

    def "compile empty less"() {
        expect:
        compiler.compile('') == ''
    }

    def "compile css"() {
        def css = 'div {\n  color: black;\n}\n'

        expect:
        compiler.compile(css) == css
    }

    def "default constructor"() {
        def compiler = new LessCompilerImpl(new RhinoLessCompilationEngine())

        expect:
        compiler.compile(new File('src/test/resources/less/basic.less')) == new File('src/test/resources/less/basic.css').text
    }

    def "constructor with custom javascript"() {
        def compiler = new LessCompilerImpl(new RhinoLessCompilationEngine(), new File('src/test/resources/less.js-tests/functions.js').text)

        expect:
        compiler.compile(new File('src/test/resources/less/basic.less')) == 'p {\n  color: #000000;\n  width: 2;\n}\n'
    }

    def "compile to file"() {
        compiler.compile(new File('src/test/resources/less/import.less'), new File('build/tmp/import.css'))

        expect:
        new File('build/tmp/import.css').text == new File('src/test/resources/less/import.css').text
    }

    def "compile file with imports"() {
        def file = new File('src/test/resources/less/import.less')
        def result = compiler.compileWithDetails(file.text, new FileSystemResourceReader(file.parentFile), new Options(), file.name, null)

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
                'in broken.less at line 1\n' +
                'extract\n' +
                '#broken less {'
    }

    def "compile file with missing imports"() {
        when:
        compiler.compile(new File('src/test/resources/less/brokenImport.less'))

        then:
        def e = thrown(Exception)
        e.message == 'less parse exception: \'doesNotExist.less\' wasn\'t found\n' +
                'in brokenImport.less at line 5\n' +
                'extract\n' +
                '@import "doesNotExist";'
    }

    def "compile and minify"() {
        def minified = compiler.compile(new File('src/test/resources/less/minify.less'), new Options(minify: true))

        expect:
        minified == 'p{background-color:#000;margin-left:1px;margin-bottom:1px;margin-top:1px;margin-right:1px}'
    }

    @Unroll
    def "#lessFile.name compatibility test"() {
        expect:
        compiler.compile(lessFile.text, new FileSystemResourceReader(lessFile.parentFile, lessFile.parentFile.parentFile), new Options(strictMath: true, relativeUrls: true), lessFile.name) == getCss(lessFile).text

        where:
        lessFile << new File('src/test/resources/less.js-tests/less').listFiles().findAll { it.name.endsWith('.less') }
    }

    def "less.js compression compatibility test"() {
        def lessFile = new File('src/test/resources/less.js-tests/less/compression/compression.less')

        expect:
        compiler.compile(lessFile, new Options(compress: true)) == getCss(lessFile, 'compression/').text
    }

    @Unroll
    def "less.js debug compatibility test #dumpLineNumbers"() {
        def lessFile = new File('src/test/resources/less.js-tests/less/debug/linenumbers.less')

        expect:
        compiler.compile(lessFile, new Options(dumpLineNumbers: dumpLineNumbers)) == getCss(lessFile, 'debug/', '-' + dumpLineNumbers.optionString).text
                .replace('{pathimport}', 'import/').replace('{path}', '')
                .replace('{pathimportesc}', 'import\\/').replace('{pathesc}', '')

        where:
        dumpLineNumbers << [COMMENTS, MEDIA_QUERY, ALL]
    }

    def "less.js global vars compatibility test"() {
        expect:
        compiler.compile(lessFile, new Options(globalVars: getJson(lessFile))) == getCss(lessFile, 'globalVars/').text.replace('/**\n  * Test\n  */\n', '')

        where:
        lessFile << new File('src/test/resources/less.js-tests/less/globalVars').listFiles().findAll {
            it.name.endsWith('.less')
        }
    }

    def "less.js legacy compatibility test"() {
        def lessFile = new File('src/test/resources/less.js-tests/less/legacy/legacy.less')

        expect:
        compiler.compile(lessFile) == getCss(lessFile, 'legacy/').text
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
        when:
        println lessFile
        println(compiler.compile(lessFile, new Options(modifyVars: getJson(lessFile), sourceMap: true)))
        println '***'

        then:
        true

        where:
        lessFile << new File('src/test/resources/less.js-tests/less/sourcemaps').listFiles().findAll {
            it.name.endsWith('.less')
        }
    }

    def "less.js static urls compatibility test"() {
        def lessFile = new File('src/test/resources/less.js-tests/less/static-urls/urls.less')

        expect:
        compiler.compile(lessFile, new Options(relativeUrls: false, rootpath: 'folder (1)/')) == getCss(lessFile, 'static-urls/').text
    }

    def getCss(File lessFile, prefix = '', suffix = '') {
        return new File(new File('src/test/resources/less.js-tests/css'), prefix + lessFile.getName().replace('.less', suffix + '.css'))
    }

    def getJson(File lessFile) {
        def file = new File(lessFile.parentFile, lessFile.name.replace('.less', '.json'))
        return new JsonSlurper().parse(file.newReader())
    }

//    def "test a single compatibility test"() {
//        def lessFile = new File('src/test/resources/less.js-tests/less/urls.less')
//
//        expect:
//        compiler.compile(lessFile.text, new FileSystemResourceReader(lessFile.parentFile, lessFile.parentFile.parentFile), new Options(strictMath: true, relativeUrls: true), lessFile.name) == getCss(lessFile).text
//    }
}
