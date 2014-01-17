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

import org.apache.commons.cli.ParseException
import spock.lang.Unroll

import static com.github.houbie.lesscss.Options.LineNumbersOutput.ALL
import static com.github.houbie.lesscss.Options.LineNumbersOutput.NONE

class LesscCommandLineParserSpec extends OutputCapturingSpec {
    LesscCommandLineParser commandLineParser = new LesscCommandLineParser('versionInfo')

    def 'check compiler options'() {
        when:
        def args = commandLine.split(' ')

        then:
        !commandLineParser.parse(args)
        commandLineParser.options == options

        where:
        commandLine                                                                                                                             | options
        'src/test/resources/less/basic.less'                                                                                                    | new Options(
                dumpLineNumbers: NONE, rootPath: '', relativeUrls: false, strictImports: false, compress: false, minify: false, optimizationLevel: 1, dependenciesOnly: false)
        '-rp rootpath -ru -x -O10 src/test/resources/less/basic.less'                                                                           | new Options(
                dumpLineNumbers: NONE, rootPath: 'rootpath', relativeUrls: true, strictImports: false, compress: true, minify: false, optimizationLevel: 10, dependenciesOnly: false)
        '--rootpath rootpath --relative-urls --compress --optimization 10 src/test/resources/less/basic.less'                                   | new Options(
                dumpLineNumbers: NONE, rootPath: 'rootpath', relativeUrls: true, strictImports: false, compress: true, minify: false, optimizationLevel: 10, dependenciesOnly: false)
        '--line-numbers all --strict-imports --strict-math --strict-units --yui-compress -M src/test/resources/less/basic.less destination.css' | new Options(
                dumpLineNumbers: ALL, rootPath: '', relativeUrls: false, strictImports: true, strictMath: true, strictUnits: true, compress: false, minify: true, optimizationLevel: 1, dependenciesOnly: true)
        '--depends src/test/resources/less/basic.less destination.css'                                                                          | new Options(
                dumpLineNumbers: NONE, rootPath: '', relativeUrls: false, strictImports: false, compress: false, minify: false, optimizationLevel: 1, dependenciesOnly: true)
    }

    def 'check source and destination'() {
        when:
        def args = commandLine.split(' ')

        then:
        !commandLineParser.parse(args)
        commandLineParser.sourceLocation == sourceLocation
        commandLineParser.destination == destination

        where:
        commandLine                                          | sourceLocation                       | destination
        'src/test/resources/less/basic.less'                 | 'src/test/resources/less/basic.less' | null
        'src/test/resources/less/basic.less destination.css' | 'src/test/resources/less/basic.less' | new File('destination.css')
    }

    def 'no source throws exception'() {
        when:
        commandLineParser.parse(['-x'] as String[])

        then:
        def e = thrown(ParseException)
        e.message == '<source> is not specified'
    }

    def 'no readable source throws exception'() {
        when:
        commandLineParser.parse(['doesNotExist'] as String[])

        then:
        def e = thrown(ParseException)
        e.message == 'doesNotExist can not be found. Check the location or use --include-path'
    }

    def 'daemon option requires destination to be set'() {
        when:
        commandLineParser.parse(['--daemon', 'src/test/resources/less/basic.less'] as String[])

        then:
        def e = thrown(ParseException)
        e.message == 'option --daemon requires an output path to be specified'
    }

    def 'daemon option is incompatible with depends option'() {
        when:
        commandLineParser.parse(['--daemon', '--depends', 'src/test/resources/less/basic.less', 'destination.css'] as String[])

        then:
        def e = thrown(ParseException)
        e.message == 'option --daemon cannot be used together with -M or --depends option'
    }

    @Unroll
    def 'check include-paths'() {
        when:
        def args = commandLine.split(' ')

        then:
        !commandLineParser.parse(args)
        commandLineParser.resourceReader.baseDirs == baseDirs*.getAbsoluteFile()
        commandLineParser.resourceReader.encoding == encoding

        where:
        commandLine                                                                      | baseDirs                                                    | encoding
        'src/test/resources/less/basic.less'                                             | [new File('src/test/resources/less').absoluteFile]          | null
        '--include-path path1:path2:/path/3 -e utf16 src/test/resources/less/basic.less' | [new File('path1'), new File('path2'), new File('/path/3')] | 'utf16'
        '--include-path path1;path2;/path/3 -e utf16 src/test/resources/less/basic.less' | [new File('path1'), new File('path2'), new File('/path/3')] | 'utf16'
        '--include-path path1 --encoding utf8 src/test/resources/less/basic.less'        | [new File('path1')]                                         | 'utf8'
    }

    @Unroll
    def 'check engine'() {
        when:
        def args = commandLine.split(' ')

        then:
        !commandLineParser.parse(args)
        commandLineParser.engine == engine

        where:
        commandLine                                                 | engine
        'src/test/resources/less/basic.less'                        | null
        '--engine jav8 -e utf16 src/test/resources/less/basic.less' | 'jav8'
    }

    def 'check encoding'() {
        when:
        commandLineParser.parse('-e utf16 src/test/resources/less/basic.less'.split(' '))

        then:
        commandLineParser.encoding == 'utf16'
    }

    def 'test custom-js'() {
        when:
        commandLineParser.parse('-js src/test/resources/less.js-tests/functions.js src/test/resources/less/basic.less'.split(' '))

        then:
        commandLineParser.customJsReader.text == new File('src/test/resources/less.js-tests/functions.js').text
    }

    def 'check version output'() {
        expect:
        commandLineParser.parse(['--version'] as String[])
        //parse should return true to signal there's nothing left to do
        sysOutCapture.toString() == 'versionInfo\n'
    }

    def 'check help output'() {
        expect:
        commandLineParser.parse(['--help'] as String[]) //parse should return true to signal there's nothing left to do
        sysOutCapture.toString() == 'usage: lessc\n' +
                '[option option=parameter ...] <source> [destination]\n' +
                '    --cache-dir <arg>      Cache directory.\n' +
                '    --daemon               Start compiler daemon.\n' +
                ' -e,--encoding <arg>       Character encoding.\n' +
                '    --engine <arg>         JavaScript engine, either \'rhino\' (default),\n' +
                '                           \'nashorn\' (requires JDK8) or \'jav8\' (only on\n' +
                '                           supported operating systems).\n' +
                ' -h,--help                 Print help (this message) and exit.\n' +
                '    --include-path <arg>   Set include paths. Separated by \':\'. Use \';\' on\n' +
                '                           Windows.\n' +
                ' -js,--custom-js <arg>     File with custom JavaScript functions.\n' +
                '    --line-numbers <arg>   --line-numbers=TYPE  Outputs filename and line\n' +
                '                           numbers.  (1)\n' +
                ' -M,--depends              Output a makefile import dependency list to\n' +
                '                           stdout.\n' +
                ' -O,--optimization <arg>   -O1, -O2... Set the parser\'s optimization\n' +
                '                           level.   (3)\n' +
                ' -rp,--rootpath <arg>      Set rootpath for URL rewriting in relative\n' +
                '                           imports and URLs.  (2)\n' +
                ' -ru,--relative-urls       Re-write relative URLs to the base less file.\n' +
                ' -s,--silent               Suppress output of error messages.\n' +
                '    --strict-imports       Force evaluation of imports.\n' +
                '    --strict-math          Use strict math.\n' +
                '    --strict-units         Use strict units.\n' +
                ' -v,--version              Print version number and exit.\n' +
                '    --verbose              Be verbose.\n' +
                ' -x,--compress             Compress output by removing some whitespaces.\n' +
                '    --yui-compress         Compress output using YUI cssmin.\n' +
                '\n' +
                '(1) --line-numbers TYPE can be:\n' +
                'comments: output the debug info within comments.\n' +
                'mediaquery: outputs the information within a fake media query which is\n' +
                'compatible with the SASS format.\n' +
                'all: does both\n' +
                '(2) --rootpath: works with or without the relative-urls option.\n' +
                '(3) Optimization levels: The lower the number, the fewer nodes created in\n' +
                'the tree. Useful for debugging or if you need to access the individual\n' +
                'nodes in the tree.\n'
    }
}
