package com.github.houbie.lesscss

class LesscSpec extends OutputCapturingSpec {

    def 'compile to destination'() {
        setup:
        Lessc.main('src/test/resources/less/basic.less build/tmp/lessc.css'.split(' '))

        expect:
        new File('build/tmp/lessc.css').text == new File('src/test/resources/less/basic.css').text
        sysOutCapture.toString() == ''
    }

    def 'compile to destination with --verbose'() {
        setup:
        Lessc.main('--verbose src/test/resources/less/basic.less build/tmp/lessc.css'.split(' '))

        expect:
        new File('build/tmp/lessc.css').text == new File('src/test/resources/less/basic.css').text
        sysOutCapture.toString() == 'start less compilation\n' +
                'prepareScriptEngine\n' +
                'Using implementation version: Rhino 1.7 release 4 2012 06 18\n' +
                'finished less compilation\n'
    }

    def 'compile to stdout with --verbose'() {
        setup:
        Lessc.main('--verbose src/test/resources/less/basic.less'.split(' '))

        expect:
        sysOutCapture.toString() == new File('src/test/resources/less/basic.css').text
    }

    def 'compile with errors'() {
        setup:
        Lessc.main('src/test/resources/less/broken.less'.split(' '))

        expect:
        sysOutCapture.toString() == ''
        sysErrCapture.toString() == 'less parse exception: missing closing `}`\n' +
                'in broken.less at line 1\n' +
                'extract\n' +
                '#broken less {\n'
    }
}
