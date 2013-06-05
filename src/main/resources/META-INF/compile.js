var less = window.less,
        parseException,

        compile = function (source) {
            var result,
                    prop,
                    rootPath = String(compilerOptions.rootPath),
                    lessEnv = {
                        compress: compilerOptions.compress,
                        optimization: compilerOptions.optimizationLevel,
                        strictImports: compilerOptions.strictImports,
                        relativeUrls: compilerOptions.relativeUrls,
                        filename: compilerOptions.fileName,
                        paths: []
                    };

            if (rootPath.length > 0) {
                lessEnv.rootpath = rootPath;
            }
            if (compilerOptions.dumpLineNumbers.getOptionString()) {
                lessEnv.dumpLineNumbers = String(compilerOptions.dumpLineNumbers.getOptionString());
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
                lessEnv,
                rootPath = String(compilerOptions.rootPath);

        clonedPaths.push(filePath);
        lessEnv = {
            compress: compilerOptions.compress,
            optimization: compilerOptions.optimizationLevel,
            strictImports: compilerOptions.strictImports,
            relativeUrls: compilerOptions.relativeUrls,
            filename: fullPath,
            paths: clonedPaths
        };

        if (compilerOptions.dumpLineNumbers.getOptionString()) {
            lessEnv.dumpLineNumbers = String(compilerOptions.dumpLineNumbers.getOptionString());
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
