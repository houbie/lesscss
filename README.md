Java LESS CSS compiler
======================

Lesscss compiles LESS code into CSS stylesheets (see <http://lesscss.org>)

Lesscss is compatible with LESS version 1.4.1. In fact, Lesscss uses the Rhino JavaScript engine to compile the official
JavaScript LESS compiler into Java byte code.

In daemon mode, Lesscss monitors your LESS files (and all their imports), and automatically compiles them when necessary.
You can see the results almost immediately in your browser.

Lesscss can be used at the commandline, but it also provides a simple API for embedded usage in build tools.


## Why yet another Java LESS compiler?

* Lesscss can start a daemon that automatically starts a compilation when the source is modified.
  Lesscss caches information about imported source files so that an initial compilation to gather imports is rarely necessary.
* Lesscss supports all options, which is not the case for the existing Java implementations.
* Lesscss is up to 5 times faster then existing Java implementations.


## Download and installation

Lesscss requires Java 1.6 or higher.

You can download Lesscss from the [maven central repository](http://central.maven.org/maven2/com/github/houbie/lesscss/0.8-less-1.4.1/lesscss-0.8-less-1.4.1.zip)
and un-zip it.

Or you can declare it as dependency in your project:

* Gradle, grab, grails, etc.: `build "com.github.houbie:lesscss:0.8-less-1.4.1"`
* Maven:

        <dependency>
          <groupId>com.github.houbie</groupId>
          <artifactId>lesscss</artifactId>
          <version>0.8-less-1.4.1</version>
        </dependency>

## Commandline usage

The zip distribution contains OS specific shell scripts that are compatible with the official lessc command.

Just type `lessc source.less destination.css` to compile a LESS file.

The complete syntax is `lessc [option option=parameter ...] <source> [destination]`

Type `lessc -h` to see the full list of options.

## Lesscss API

You can create a `CompilationTask` instance to compile one or more LESS files (Groovy example):

    CompilationUnit bootstrap= newCompilationUnit('less/bootstrap.less', 'css/bootstrap.css', new Options(), new FileSystemResourceReader('less'))
    CompilationUnit myLess= newCompilationUnit('src/myLess.less', 'css/myLess.css')
    CompilationTask compilationTask = new CompilationTask(cacheDir: new File('/tmp'), compilationUnits: [bootstrap, myLess])
    compilationTask.execute() //only once
    compilationTask.startDaemon(100) //check every 100 milliseconds for changes
    ...
    compilationTask.stopDaemon()

You can also use the LessCompiler directly (again Groovy):

    //simple usage
    new LessCompilerImpl().compile(new File('source'), new File('destination'))

    //full option API
    LessCompiler.CompilationDetails details= new LessCompilerImpl(new File('customJavaScriptFunctions.js').text).compileWithDetails(
        new File('source.less').text,
        new FileSystemResourceReader('importsDir'),
        new Options(minify: true),
        'source.less')

    println details.result //generated CSS
    println deatils.imports //list of imports encountered during compilation

## Compatibility

Lesscss passes all the tests of the official JavaScript LESS 1.4.1 compiler, except the test for _data-uri_.
Lesscss handles _data-uri_ the same way as the official LESS does when used inside a browser: the _data-uri_'s are translated
into URL's in stead of being embedded in the CSS.

## Building from source
If you use the included gradle wrapper, you don't have to install anything (except a JDK)

    git clone https://github.com/houbie/lesscss.git
    cd lesscss
    ./gradlew installApp

Or use gradle 1.8 or higher

Usefull gradle tasks:

* _install_ : installs the lesscss jar into the local maven repo
* _installApp_ : installs lesscss in _build/install/lesscss_
* _distZip_ : creates the zip distribution
* _clean_, _test_, _jar_, etc.

## What's up next?

Compile main LESS files and/or imports that reside in jar files. This would make it possible to use a jar'ed version of Twitter Bootstrap, without having to copy all the LESS files to your project source tree. Typically you would only need to have your own version of _variables.less_.
