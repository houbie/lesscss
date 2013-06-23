package com.github.houbie.lesscss

import spock.lang.Specification

class FileSystemResourceReaderSpec extends Specification {

    def "read file from work dir"() {
        expect:
        new FileSystemResourceReader().read('src/test/resources/ioutils/plain.txt') == 'plain text'
    }

    def "read file from work dir with encoding"() {
        expect:
        new FileSystemResourceReader('utf16').read('src/test/resources/ioutils/utf16.txt') == 'utf16 text'
    }

    def "read file from specified dir"() {
        when:
        def reader = new FileSystemResourceReader(new File('src/test/resources'))
        then:
        reader.read('ioutils/plain.txt') == 'plain text'
        reader.read('../resources/ioutils/plain.txt') == 'plain text'
    }

    def "read file from specified dir with encoding"() {
        when:
        def reader = new FileSystemResourceReader('utf16', new File('src/test/resources'))
        then:
        reader.read('ioutils/utf16.txt') == 'utf16 text'
        reader.read('../resources/ioutils/utf16.txt') == 'utf16 text'
    }

}
