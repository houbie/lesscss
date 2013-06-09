var less = window.less,
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
                        paths: []
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
                new (less.Parser)(lessEnv).parse(source, function (e, tree) {
                    if (e instanceof Object) {
                        throw e;
                    }
                    result = tree.toCSS(lessEnv.compress);
                    if (e instanceof Object)
                        throw e;
                });
                parseException = null;
                return result;
            } catch (e) {
                parseException = 'less parse exception: ';
                for (prop in e) {
                    if (e.hasOwnProperty(prop)) {
                        parseException += prop + ':' + e[prop] + ',';
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
            if (e instanceof Object)
                throw e;
            callback(e, root);
            if (e instanceof Object)
                throw e;
        });
    }
};
