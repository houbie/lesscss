package com.github.houbie.lesscss

import com.github.houbie.lesscss.resourcereader.FileSystemResourceReader
import spock.lang.Specification
import spock.lang.Unroll

import static com.github.houbie.lesscss.Options.LineNumbersOutput.*

class LessCompilerSpec extends Specification {

    static LessCompilerImpl compiler

    def setupSpec() {
        Reader reader = new File('src/test/resources/less.js-tests/functions.js').newReader()
        compiler = new LessCompilerImpl(reader)
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
        def compiler = new LessCompilerImpl()

        expect:
        compiler.compile(new File('src/test/resources/less/basic.less')) == new File('src/test/resources/less/basic.css').text
    }

    def "constructor with custom javascript"() {
        def compiler = new LessCompilerImpl(new File('src/test/resources/less.js-tests/functions.js').text)

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
                'in broken.less at line 1\n' +
                'extract\n' +
                '#broken less {'
    }

    def "compile file with missing imports"() {
        when:
        compiler.compile(new File('src/test/resources/less/brokenImport.less'))

        then:
        def e = thrown(Exception)
        e.message == 'less parse exception: less compiler error: import "doesNotExist.less" could not be resolved\n' +
                'in brokenImport.less at line null\n' +
                'extract\n' +
                '  color: red;'
    }

    def "compile and minify"() {
        def minified = compiler.compile(new File('src/test/resources/less/minify.less'), new Options(minify: true))

        expect:
        minified == 'p{background-color:#000;margin-left:1px;margin-bottom:1px;margin-top:1px;margin-right:1px}'
    }

    @Unroll
    def "less.js compatibility tests for #lessFile"() {
        expect:
        compiler.compile(lessFile, new Options(strictImports: true)) == getCss(lessFile).text

        where:
        lessFile << new File('src/test/resources/less.js-tests/less').listFiles().findAll { it.name.endsWith('.less') }
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

    def "less.js static urls compatibility test"() {
        def lessFile = new File('src/test/resources/less.js-tests/less/static-urls/urls.less')
        def compiler = compiler

        expect:
        compiler.compile(lessFile, new Options(relativeUrls: false, rootPath: 'folder (1)/')) == getCss(lessFile, 'static-urls/').text
    }

    def getCss(File lessFile, prefix = '', suffix = '') {
        return new File(new File('src/test/resources/less.js-tests/css'), prefix + lessFile.getName().replace('.less', suffix + '.css'))
    }
}
