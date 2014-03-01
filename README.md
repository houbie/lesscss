Java LESS CSS compiler
======================

Lesscss compiles LESS code into CSS stylesheets (see <http://lesscss.org>)

Lesscss is compatible with LESS version 1.7.0. In fact, Lesscss executes the official JavaScript LESS compiler in a JVM.

In daemon mode, Lesscss monitors your LESS files (and all their imports), and automatically compiles them when necessary.
So you only need to save and refresh your browser.

Lesscss can be used at the commandline, but it also provides a simple API for embedded usage in build tools.


## Why yet another Java LESS compiler?

* Lesscss can start a daemon that automatically starts a compilation when the source is modified.
* Lesscss caches information about imported source files so that an initial compilation to gather imports is rarely necessary.
* Lesscss supports all options, which is not the case for the other Java implementations (at the time of writing).
* Lesscss is up to 5 times faster then existing Java implementations.


## Download and installation

Lesscss requires Java 1.6 or higher.

You can download Lesscss from the [maven central repository](http://central.maven.org/maven2/com/github/houbie/lesscss/1.0.0-less-1.4.1/lesscss-1.0.0-less-1.4.1.zip)
and un-zip it.

Or you can declare it as dependency in your project:

* Gradle, grab, grails, etc.: `build "com.github.houbie:lesscss:1.0.0-less-1.4.1"`
* Maven:

        <dependency>
          <groupId>com.github.houbie</groupId>
          <artifactId>lesscss</artifactId>
          <version>1.0.0-less-1.4.1</version>
        </dependency>

## Commandline usage

The zip distribution contains OS specific shell scripts that are compatible with the official lessc command.

Just type `lessc source.less destination.css` to compile a LESS file.

The complete syntax is `lessc [option option=parameter ...] <source> [destination]`

Type `lessc -h` to see the full list of options.

### Tip for compiling Twitter Bootstrap

When using Twitter Bootstrap in multiple projects, it is not necessary to copy all the LESS files to all the projects.
Only copy the ones that you want to customize (typically variables.less), and then compile with:

    lessc --include-path your/project/less:path/to/bootstrap-3.0.0/less bootstrap.less css/bootstrap.css

This will first look for less files in _your/project/less_, and when not found it will fall back to _path/to/bootstrap-3.0.0/less_

## Lesscss API

You can create a `CompilationTask` instance to compile one or more LESS files (Groovy example):

    CompilationUnit bootstrap= newCompilationUnit('less/bootstrap.less', 'css/bootstrap.css', new Options(), new FileSystemResourceReader(new File('less')))
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
    println details.imports //list of imports encountered during compilation

There are 3 _ResourceReader_ implementations available for resolving source and imported LESS files:

* _FileSystemResourceReader_: search resources in one or more directories, ex. `new FileSystemResourceReader(new File('webapp/less'), new File('/bootstrap/less'))`
* _ClasspathResourceReader_ : search resources in the classpath relative to a base path, ex. `new ClasspathResourceReader('bootstrap/less')`
* _CombiningResourceReader_ : delegates to the ResourceReader's in an array until the resource is resolved, ex. `new CombiningResourceReader(srcResourceReader, jarResourceReader)`

## Compatibility

Lesscss passes all the tests of the official JavaScript LESS 1.7.0 compiler, except the test for _data-uri_.
Lesscss handles _data-uri_ the same way as the official LESS does when used inside a browser: the _data-uri_'s are translated
into URL's in stead of being embedded in the CSS.

## Turn Lessc into a Porsche (experimental)

When invoking lessc with the  _--engine jav8_ option, it will use a super fast embedded V8 JavaScript engine.

**It can crash once in a while!**

## Building from source
If you use the included gradle wrapper, you don't have to install anything (except a JDK)

    git clone https://github.com/houbie/lesscss.git
    cd lesscss
    ./gradlew installApp

Or use gradle 1.8 or higher

Useful gradle tasks:

* _install_ : installs the lesscss jar into the local maven repo
* _installApp_ : installs lesscss in _build/install/lesscss_
* _distZip_ : creates the zip distribution
* _clean_, _test_, _jar_, etc.

## What's up next?

A Grails plugin that monitors and automatically compiles the LESS sources in dev mode and packages the generated CSS in the war.

## Wish list

A stable embedded V8 engine.
