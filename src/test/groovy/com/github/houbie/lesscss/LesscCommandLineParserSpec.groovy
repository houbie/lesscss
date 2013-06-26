package com.github.houbie.lesscss

import spock.lang.Specification

import static com.github.houbie.lesscss.Options.LineNumbersOutput.ALL
import static com.github.houbie.lesscss.Options.LineNumbersOutput.NONE

class LesscCommandLineParserSpec extends OutputCapturingSpec {
    LesscCommandLineParser commandLineHelper = new LesscCommandLineParser('versionInfo')

    def 'check compiler options'() {
        when:
        def args = commandLine.split(' ')

        then:
        !commandLineHelper.parse(args)
        commandLineHelper.options == options

        where:
        commandLine                                                                                                | options
        'src/test/resources/less/basic.less'                                                                       | new Options(
                dumpLineNumbers: NONE, rootPath: '', relativeUrls: false, strictImports: false, compress: false, minify: false, optimizationLevel: 1, dependenciesOnly: false)
        '-rp rootpath -ru -x -O10 src/test/resources/less/basic.less'                                              | new Options(
                dumpLineNumbers: NONE, rootPath: 'rootpath', relativeUrls: true, strictImports: false, compress: true, minify: false, optimizationLevel: 10, dependenciesOnly: false)
        '--rootpath rootpath --relative-urls --compress --optimization 10 src/test/resources/less/basic.less'      | new Options(
                dumpLineNumbers: NONE, rootPath: 'rootpath', relativeUrls: true, strictImports: false, compress: true, minify: false, optimizationLevel: 10, dependenciesOnly: false)
        '--line-numbers all --strict-imports --yui-compress -M src/test/resources/less/basic.less destination.css' | new Options(
                dumpLineNumbers: ALL, rootPath: '', relativeUrls: false, strictImports: true, compress: false, minify: true, optimizationLevel: 1, dependenciesOnly: false)
        '--depends src/test/resources/less/basic.less destination.css'                                             | new Options(
                dumpLineNumbers: NONE, rootPath: '', relativeUrls: false, strictImports: false, compress: false, minify: false, optimizationLevel: 1, dependenciesOnly: true)
    }

    def 'check source and destination'() {
        when:
        def args = commandLine.split(' ')

        then:
        !commandLineHelper.parse(args)
        commandLineHelper.source == source
        commandLineHelper.destination == destination

        where:
        commandLine                                          | source                                         | destination
        'src/test/resources/less/basic.less'                 | new File('src/test/resources/less/basic.less') | null
        'src/test/resources/less/basic.less destination.css' | new File('src/test/resources/less/basic.less') | new File('destination.css')
    }

    def 'no source throws exception'() {
        when:
        commandLineHelper.parse(['-x'] as String[])

        then:
        def e = thrown(RuntimeException)
        e.message == '<source> is not specified'
    }

    def 'no readable source throws exception'() {
        when:
        commandLineHelper.parse(['doesNotExist'] as String[])

        then:
        def e = thrown(RuntimeException)
        e.message == 'doesNotExist can not be read'
    }

    def 'depends option requires destination to be set'() {
        when:
        commandLineHelper.parse(['-M', 'src/test/resources/less/basic.less'] as String[])

        then:
        def e = thrown(RuntimeException)
        e.message == 'option --depends requires an output path to be specified'
    }

    def 'check include-paths'() {
        when:
        def args = commandLine.split(' ')

        then:
        !commandLineHelper.parse(args)
        commandLineHelper.includePathsReader.baseDirs == baseDirs
        commandLineHelper.includePathsReader.encoding == encoding

        where:
        commandLine                                                                      | baseDirs                                                    | encoding
        'src/test/resources/less/basic.less'                                             | [new File('src/test/resources/less')]                       | null
        '--include-path path1,path2;/path/3 -e utf16 src/test/resources/less/basic.less' | [new File('path1'), new File('path2'), new File('/path/3')] | 'utf16'
        '--include-path path1 --encoding utf8 src/test/resources/less/basic.less'        | [new File('path1')]                                         | 'utf8'
    }

    def 'check encoding'() {
        when:
        commandLineHelper.parse('-e utf16 src/test/resources/less/basic.less'.split(' '))

        then:
        commandLineHelper.encoding == 'utf16'
    }

    def 'test custom-js'() {
        when:
        commandLineHelper.parse('-js src/test/resources/less.js-tests/functions.js src/test/resources/less/basic.less'.split(' '))

        then:
        commandLineHelper.customJsReader.text == new File('src/test/resources/less.js-tests/functions.js').text
    }

    def 'check version output'() {
        expect:
        commandLineHelper.parse(['--version'] as String[]) //parse should return true to signal there's nothing left to do
        sysOutCapture.toString() == 'versionInfo\n'
    }

    def 'check help output'() {
        expect:
        commandLineHelper.parse(['--help'] as String[]) //parse should return true to signal there's nothing left to do
        sysOutCapture.toString() == 'usage: lessc\n' +
                '[option option=parameter ...] <source> [destination]\n' +
                ' -e,--encoding <arg>       Character encoding.\n' +
                ' -h,--help                 Print help (this message) and exit.\n' +
                '    --include-path <arg>   Set include paths.  (3)\n' +
                ' -js,--custom-js <arg>     File with custom JavaScript functions.\n' +
                '    --line-numbers <arg>   --line-numbers=TYPE  Outputs filename and line\n' +
                '                           numbers.  (1)\n' +
                ' -M,--depends              Output a makefile import dependency list to\n' +
                '                           stdout.\n' +
                ' -O,--optimization <arg>   -O1, -O2... Set the parser\'s optimization\n' +
                '                           level.   (4)\n' +
                ' -rp,--rootpath <arg>      Set rootpath for URL rewriting in relative\n' +
                '                           imports and URLs.  (2)\n' +
                ' -ru,--relative-urls       Re-write relative URLs to the base less file.\n' +
                ' -s,--silent               Suppress output of error messages.\n' +
                '    --strict-imports       Force evaluation of imports.\n' +
                ' -v,--version              Print version number and exit.\n' +
                '    --verbose              Be verbose.\n' +
                ' -x,--compress             Compress output by removing some whitespaces.\n' +
                '    --yui-compress         Compress output using YUI cssmin.\n' +
                '\n' +
                '(1) --line-numbers TYPE can be:\n' +
                'comments: output the debug info within comments.\n' +
                'mediaquery: outputs the information within a fake media query which is\n' +
                'compatible with the SASS format.\n' +
                'all does both\n' +
                '(2) --rootpath: works with or without the relative-urls option.\n' +
                '(3) --include-path: separated by : or ;\n' +
                '(4) Optimization levels: The lower the number, the fewer nodes created in\n' +
                'the tree. Useful for debugging or if you need to access the individual\n' +
                'nodes in the tree.\n'
    }
}
