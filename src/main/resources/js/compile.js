var less = window.less,
        originalException,
        parseException,
        options,
        importReader,

        compile = function (source, optionsArg, sourceName, importReaderArg) {
            var result,
                    prop,
                    rootPath = String(optionsArg.rootPath),
                    lessEnv = {
                        compress: optionsArg.compress,
                        optimization: optionsArg.optimizationLevel,
                        strictImports: optionsArg.strictImports,
                        relativeUrls: optionsArg.relativeUrls,
                        filename: sourceName,
                        paths: [],
                        dependenciesOnly: optionsArg.dependenciesOnly
                    };
            options = optionsArg;
            importReader = importReaderArg;

            if (rootPath.length > 0) {
                lessEnv.rootpath = rootPath;
            }
            if (options.dumpLineNumbers.getOptionString()) {
                lessEnv.dumpLineNumbers = String(options.dumpLineNumbers.getOptionString());
            }

            try {
                parseException = null;
                originalException = null;
                new (less.Parser)(lessEnv).parse(source, function (e, tree) {
                    originalException = originalException || e;
                    if (originalException instanceof Object) {
                        throw originalException;
                    }
                    result = (lessEnv.dependenciesOnly) ? '' : tree.toCSS(lessEnv.compress);
                });
                if (originalException) {
                    throw originalException;
                }
                return (optionsArg.minify) ? cssmin(result) : result;
            } catch (e) {
                originalException = originalException || e;

                parseException = 'less parse exception: ' + originalException.message;
                if (originalException.filename) {
                    parseException += '\nin ' + originalException.filename + ' at line ' + originalException.line;
                }
                if (originalException.extract) {
                    var extract = originalException.extract;
                    parseException += '\nextract';
                    for (var line in extract) {
                        if (extract.hasOwnProperty(line) && extract[line]) {
                            parseException += '\n' + originalException.extract[line];
                        }
                    }
                }
                return null;
            }
        };

less.Parser.importer = function (file, paths, callback) {
    if (file != null) {
        var fullPath = paths.join('') + file,
                clonedPaths = paths.slice(0),
                filePath = file.substring(0, file.lastIndexOf('/') + 1),
                importedLess = importReader.read(fullPath),
                rootPath = String(options.rootPath),
                lessEnv;

        clonedPaths.push(filePath);
        lessEnv = {
            compress: options.compress,
            optimization: options.optimizationLevel,
            strictImports: options.strictImports,
            relativeUrls: options.relativeUrls,
            filename: fullPath,
            paths: clonedPaths
        };

        if (options.dumpLineNumbers.getOptionString()) {
            lessEnv.dumpLineNumbers = String(options.dumpLineNumbers.getOptionString());
        }

        if (rootPath.length > 0) {
            lessEnv.rootpath = rootPath + ((lessEnv.relativeUrls) ? clonedPaths.join('') : '');
        } else if (lessEnv.relativeUrls) {
            lessEnv.rootpath = clonedPaths.join('');
        }

        if (importedLess == null) {
            throw {name: 'less import error', message: 'less compiler error: import "' + fullPath + '" could not be resolved'};
        }

        new (less.Parser)(lessEnv).parse(String(importedLess), function (e, root) {
            if (e instanceof Object) {
                originalException = originalException || e;
            } else {
                callback(e, root);
            }
        });
    }
};
