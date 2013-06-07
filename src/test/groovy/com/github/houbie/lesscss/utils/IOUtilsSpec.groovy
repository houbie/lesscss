package com.github.houbie.lesscss.utils

import spock.lang.Specification

class IOUtilsSpec extends Specification {

    def "read a file without encoding"() {
        expect:
        IOUtils.read(new File('src/test/resources/ioutils/plain.txt')) == 'plain text'
    }

    def "read a file with encoding"() {
        expect:
        IOUtils.read(new File('src/test/resources/ioutils/utf16.txt'), 'utf16') == 'utf16 text'
    }

    def "read a url without encoding"() {
        expect:
        IOUtils.read(IOUtils.classLoader.getResource('ioutils/plain.txt')) == 'plain text'
    }

    def "read a url with encoding"() {
        expect:
        IOUtils.read(IOUtils.classLoader.getResource('ioutils/utf16.txt'), 'utf16') == 'utf16 text'
    }

    def "write file with default encoding"() {
        def file = new File('build/tmp/ioutilstest.txt')
        IOUtils.writeFile('content', file, null)

        expect:
        file.text == 'content'
    }

    def "write file UTF16"() {
        def file = new File('build/tmp/ioutilstest.txt')
        IOUtils.writeFile('content', file, 'UTF16')

        expect:
        file.getText('UTF16') == 'content'
    }
}
