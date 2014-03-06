package com.github.houbie.lesscss

import spock.lang.Specification

class OptionsSpec extends Specification {
    static def nonDefaultPropertyValues = [
            compress: true,
            optimizationLevel: 2,
            strictImports: true,
            rootpath: 'rootpath',
            relativeUrls: true,
            dumpLineNumbers: Options.LineNumbersOutput.ALL,
            dependenciesOnly: true,
            strictMath: true,
            strictUnits: true,
            ieCompat: false,
            javascriptEnabled: false,
            lint: true,
            silent: true,
            sourceMap: true,
            sourceMapRootpath: 'sourceMapRootpath',
            sourceMapBasepath: 'sourceMapBasepath',
            sourceMapLessInline: 'sourceMapLessInline',
            sourceMapMapInline: 'sourceMapMapInline',
            sourceMapURL: 'sourceMapUrl',
            globalVars: [globalVars: 'globalVarValue'],
            modifyVars: [modifyVars: 'modifyVarValue'],
            minify: true
    ]

    def 'check test data'() {
        def options = new Options()

        expect:
        nonDefaultPropertyValues.size() == options.properties.size() - 1 //properties includes class
    }

    def "changing a property changes equals and hashCode"() {
        when:
        def defaultOptions = new Options()
        def modifiedOptions = new Options()
        modifiedOptions[prop] = nonDefaultPropertyValues[prop]

        then:
        modifiedOptions != defaultOptions
        modifiedOptions.hashCode() != defaultOptions.hashCode()

        where:
        prop << nonDefaultPropertyValues.keySet()
    }

    def 'copy constructor'() {
        when:
        def options = new Options()
        options[prop] = nonDefaultPropertyValues[prop]
        def copy = new Options(options)

        then:
        copy == options
        copy.hashCode() == options.hashCode()

        where:
        prop << nonDefaultPropertyValues.keySet()
    }

}
