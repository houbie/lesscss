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

class LesscCommandLineParserSpec extends OutputCapturingSpec {
    LesscCommandLineParser commandLineParser = new LesscCommandLineParser('versionInfo')

//    @Unroll
    def 'check compiler options'() {
        when:
        def args = commandLine.split(' ')

        then:
        !commandLineParser.parse(args)
        commandLineParser.options == options

        where:
        commandLine                                                                                              | options
        'src/test/resources/less/basic.less'                                                                     | new Options()
        'src/test/resources/less/basic.less -x'                                                                  | new Options(compress: true)
        'src/test/resources/less/basic.less --compress'                                                          | new Options(compress: true)
        'src/test/resources/less/basic.less -O1'                                                                 | new Options(optimizationLevel: 1)
        'src/test/resources/less/basic.less --strict-imports '                                                   | new Options(strictImports: true)
        'src/test/resources/less/basic.less -rp=rootpathUrl'                                                     | new Options(rootpath: 'rootpathUrl')
        'src/test/resources/less/basic.less --rootpath=rootpathUrl'                                              | new Options(rootpath: 'rootpathUrl')
        'src/test/resources/less/basic.less -ru'                                                                 | new Options(relativeUrls: true)
        'src/test/resources/less/basic.less --relative-urls'                                                     | new Options(relativeUrls: true)
        'src/test/resources/less/basic.less --line-numbers=comments'                                             | new Options(dumpLineNumbers: Options.LineNumbersOutput.COMMENTS)
        'src/test/resources/less/basic.less --M'                                                                 | new Options(dependenciesOnly: true)
        'src/test/resources/less/basic.less --depends'                                                           | new Options(dependenciesOnly: true)
        'src/test/resources/less/basic.less -sm'                                                                 | new Options(strictMath: true)
        'src/test/resources/less/basic.less --strict-math'                                                       | new Options(strictMath: true)
        'src/test/resources/less/basic.less -su'                                                                 | new Options(strictUnits: true)
        'src/test/resources/less/basic.less --strict-units'                                                      | new Options(strictUnits: true)
        'src/test/resources/less/basic.less --no-ie-compat'                                                      | new Options(ieCompat: false)
        'src/test/resources/less/basic.less --no-js'                                                             | new Options(javascriptEnabled: false)
        'src/test/resources/less/basic.less -l'                                                                  | new Options(lint: true)
        'src/test/resources/less/basic.less --lint'                                                              | new Options(lint: true)
        'src/test/resources/less/basic.less -s'                                                                  | new Options(silent: true)
        'src/test/resources/less/basic.less --silent'                                                            | new Options(silent: true)
        'src/test/resources/less/basic.less --source-map=sourceMapFileName'                                      | new Options(sourceMap: true, sourceMapFileName: 'sourceMapFileName')
        'src/test/resources/less/basic.less --source-map-rootpath=sourceMapRootpath'                             | new Options(sourceMapRootpath: 'sourceMapRootpath')
        'src/test/resources/less/basic.less --source-map-basepath=sourceMapBasePath'                             | new Options(sourceMapBasepath: 'sourceMapBasePath')
        'src/test/resources/less/basic.less --source-map-less-inline'                                            | new Options(sourceMapLessInline: true)
        'src/test/resources/less/basic.less --source-map-map-inline'                                             | new Options(sourceMapMapInline: true)
        'src/test/resources/less/basic.less --source-map-url=sourceMapUrl'                                       | new Options(sourceMapUrl: 'sourceMapUrl')
        'src/test/resources/less/basic.less --global-var=\'var1=value1\' --global-var=\'var2=value2\''           | new Options(globalVars: [var1: 'value1', var2: 'value2',])
        'src/test/resources/less/basic.less --modify-var=\'var1=value1\' --modify-var=\'var2=value2\''           | new Options(modifyVars: [var1: 'value1', var2: 'value2',])
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
                '    --cache-dir <arg>             Cache directory.\n' +
                '    --daemon                      Start compiler daemon.\n' +
                ' -e,--encoding <arg>              Character encoding.\n' +
                '    --engine <arg>                JavaScript engine, either \'rhino\'\n' +
                '                                  (default), \'nashorn\' (requires JDK8) or\n' +
                '                                  \'jav8\' (only on supported operating\n' +
                '                                  systems).\n' +
                '    --global-var <arg>            --global-var=\'VAR=VALUE\' Defines a\n' +
                '                                  variable that can be referenced by the\n' +
                '                                  file.\n' +
                ' -h,--help                        Print help (this message) and exit.\n' +
                '    --include-path <arg>          Set include paths. Separated by `:\'. Use\n' +
                '                                  `;\' on Windows.\n' +
                ' -js,--custom-js <arg>            File with custom JavaScript functions.\n' +
                ' -l,--lint                        Syntax check only (lint).\n' +
                '    --line-numbers <arg>          [deprecated] --line-numbers=TYPE Outputs\n' +
                '                                  filename and line numbers. TYPE can be\n' +
                '                                  either \'comments\', which will output the\n' +
                '                                  debug info within comments, \'mediaquery\'\n' +
                '                                  that will output the information within\n' +
                '                                  a fake media query which is compatible\n' +
                '                                  with the SASS format, and \'all\' which\n' +
                '                                  will do both.\n' +
                ' -M,--depends                     Output a makefile import dependency list\n' +
                '                                  to stdout.\n' +
                '    --modify-var <arg>            --modify-var=\'VAR=VALUE\' Modifies a\n' +
                '                                  variable already declared in the file.\n' +
                '    --no-ie-compat                Disable IE compatibility checks.\n' +
                '    --no-js                       Disable JavaScript in less files\n' +
                ' -O,--optimization <arg>          [deprecated] -O0, -O1, -O2; Set the\n' +
                '                                  parser\'s optimization level. The lower\n' +
                '                                  the number, the less nodes it will\n' +
                '                                  create in the tree. This could matter\n' +
                '                                  for debugging, or if you want to access\n' +
                '                                  the individual nodes in the tree.\n' +
                ' -rp,--rootpath <arg>             Set rootpath for url rewriting in\n' +
                '                                  relative imports and urls. Works with or\n' +
                '                                  without the relative-urls option.\n' +
                ' -ru,--relative-urls              re-write relative urls to the base less\n' +
                '                                  file.\n' +
                ' -s,--silent                      Suppress output of error messages.\n' +
                ' -sm,--strict-math                Turn on or off strict math, where in\n' +
                '                                  strict mode, math requires brackets.\n' +
                '                                  This option may default to on and then\n' +
                '                                  be removed in the future.\n' +
                '    --source-map <arg>            --source-map[=FILENAME] Outputs a v3\n' +
                '                                  sourcemap to the filename (or output\n' +
                '                                  filename.map)\n' +
                '    --source-map-basepath <arg>   Sets sourcemap base path, defaults to\n' +
                '                                  current working directory.\n' +
                '    --source-map-less-inline      puts the less files into the map instead\n' +
                '                                  of referencing them\n' +
                '    --source-map-map-inline       puts the map (and any less files) into\n' +
                '                                  the output css file\n' +
                '    --source-map-rootpath <arg>   adds this path onto the sourcemap\n' +
                '                                  filename and less file paths\n' +
                '    --source-map-url <arg>        the complete url and filename put in the\n' +
                '                                  less file\n' +
                '    --strict-imports              Force evaluation of imports.\n' +
                ' -su,--strict-units               Allow mixed units, e.g. 1px+1em or\n' +
                '                                  1px*1px which have units that cannot be\n' +
                '                                  represented.\n' +
                ' -v,--version                     Print version number and exit.\n' +
                '    --verbose                     Be verbose.\n' +
                ' -x,--compress                    Compress output by removing some\n' +
                '                                  whitespaces.\n' +
                '    --yui-compress                Compress output using YUI cssmin.\n'
    }
}
