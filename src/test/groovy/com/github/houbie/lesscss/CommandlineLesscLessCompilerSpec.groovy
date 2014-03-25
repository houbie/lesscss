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

import com.github.houbie.lesscss.engine.CommandLineLesscCompilationEngine
import com.github.houbie.lesscss.resourcereader.FileSystemResourceReader
import spock.lang.Unroll

class CommandlineLesscLessCompilerSpec extends AbstractLessCompilerSpec {

    def setupSpec() {
        compiler = new LessCompilerImpl(new CommandLineLesscCompilationEngine(System.getProperty('lesscExecutable', 'lessc')))
    }

    def "compile file with imports"() {
        def file = new File('src/test/resources/less/import.less')
        def result = compiler.compileWithDetails(file.text, new FileSystemResourceReader(file.parentFile), new Options(), file.name)

        expect:
        result.result == new File('src/test/resources/less/import.css').text
    }

    def "compile file with errors"() {
        when:
        compiler.compile(new File('src/test/resources/less/broken.less'))

        then:
        def e = thrown(Exception)
        e.message == 'ParseError: missing closing `}` in - on line 1, column 14:\n' +
                '1 #broken less {\n\n'
    }

    def "less.js modify vars compatibility test"() {
        expect:
        //first modify-var seems to be ignored on the commandline
        compiler.compile(lessFile, new Options(modifyVars: getJson(lessFile))) == getCss(lessFile, 'modifyVars/').text

        where:
        lessFile << new File('src/test/resources/less.js-tests/less/modifyVars').listFiles().findAll {
            it.name.endsWith('.less')
        }
    }

    def "less.js source maps compatibility test"() {
        def lessFile = new File('src/test/resources/less.js-tests/less/sourcemaps/basic.less')
        expect:
        // use inline less and only ignore differences in file paths in "sources":[...]
        compiler.compileWithDetails(lessFile.text, new FileSystemResourceReader(lessFile.parentFile),
                new Options(modifyVars: getJson(lessFile), sourceMap: true, sourceMapRootpath: 'testweb/sourcemaps/', sourceMapBasepath: new File('src/test/resources/less.js-tests'), sourceMapLessInline: true),
                lessFile.name, 'sourcemaps/basic.css', 'build/tmp/commandline/basic.map').sourceMap.endsWith(new File('src/test/resources/less.js-tests/sourcemaps/commandline-sourcemap.map').text)
    }

    def "compile file with missing imports"() {
        when:
        compiler.compile(new File('src/test/resources/less/brokenImport.less'))

        then:
        def e = thrown(Exception)
        e.message == 'FileError: \'doesNotExist.less\' wasn\'t found in - on line 5, column 1:\n' +
                '4 \n' +
                '5 @import "doesNotExist";\n\n'
    }

    @Unroll
    def "#lessFile.name compatibility test"() {
        expect:
        compiler.compile(lessFile.text, new FileSystemResourceReader(lessFile.parentFile, lessFile.parentFile.parentFile), new Options(strictMath: true, relativeUrls: true), lessFile.name) == getCss(lessFile).text

        where:
        lessFile << new File('src/test/resources/less.js-tests/less').listFiles().findAll {
            it.name.endsWith('.less') && !(it.name == 'functions.less' || it.name == 'urls.less')
        }
    }
}
