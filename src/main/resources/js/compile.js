var less = window.less,
        _parseException,

        _compile = function (source) {
            var result,
                    prop,
                    rootPath = String(_rootPath),
                    lessEnv = {
                        compress: _compress,
                        optimization: _optimizationLevel,
                        strictImports: _strictImports,
                        relativeUrls: _relativeUrls,
                        filename: _sourceName,
                        paths: []
                    };

            if (rootPath.length > 0) {
                lessEnv.rootpath = rootPath;
            }
            if (_dumpLineNumbers) {
                lessEnv.dumpLineNumbers = String(_dumpLineNumbers);
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
                _parseException = null;
                return result;
            } catch (e) {
                _parseException = 'less parse exception: ';
                for (prop in e) {
                    if (e.hasOwnProperty(prop)) {
                        _parseException += prop + ':' + e[prop] + ',';
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
                importedLess = _importReader.read(fullPath),
                rootPath = String(_rootPath),
                lessEnv;

        clonedPaths.push(filePath);
        lessEnv = {
            compress: _compress,
            optimization: _optimizationLevel,
            strictImports: _strictImports,
            relativeUrls: _relativeUrls,
            filename: fullPath,
            paths: clonedPaths
        };

        if (_dumpLineNumbers) {
            lessEnv.dumpLineNumbers = String(_dumpLineNumbers);
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
