var less = window.less,
        parseException,

        compile = function (source) {
            var result,
                    prop,
                    lessEnv = {
                        compress: compilerOptions.compress,
                        optimization: compilerOptions.optimizationLevel,
                        strictImports: compilerOptions.strictImports,
                        relativeUrls: compilerOptions.relativeUrls,
                        filename: compilerOptions.fileName,
                        paths: []
                    };

            if (compilerOptions.rootPath.trim().length > 0) {
                lessEnv.rootpath = compilerOptions.rootPath.trim();
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
                lessEnv;

        clonedPaths.push(filePath);
        lessEnv = {
            compress: compilerOptions.compress,
            optimization: compilerOptions.optimizationLevel,
            strictImports: compilerOptions.strictImports,
            rootpath: compilerOptions.rootPath,
            relativeUrls: compilerOptions.relativeUrls,
            filename: fullPath,
            paths: clonedPaths
        };

        if (compilerOptions.dumpLineNumbers.getOptionString()) {
            lessEnv.dumpLineNumbers = String(compilerOptions.dumpLineNumbers.getOptionString());
        }

        if (compilerOptions.relativeUrls) {
            lessEnv.rootpath += clonedPaths.join('');
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
