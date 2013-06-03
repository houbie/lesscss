var less = window.less,
        optimizationLevel,
        parseException,

        compile = function (source, compress, optimization) {
            var result,
                    prop;

            //save options for import function
            optimizationLevel = optimization;

            try {
                new (less.Parser)({ optimization: optimization, paths: [] }).parse(source, function (e, tree) {
                    if (e instanceof Object) {
                        throw e;
                    }
                    result = tree.toCSS(compress);
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
                importedLess = importReader.read(fullPath);
        clonedPaths.push(filePath);

        if (importedLess == null) {
            throw {name: 'less import error', message: 'less compiler error: import "' + fullPath + '" could not be resolved'};
        }

        new (less.Parser)({ optimization: optimizationLevel, paths: clonedPaths, rootpath: clonedPaths.join('')}).parse(String(importedLess), function (e, root) {
            if (e instanceof Object)
                throw e;
            callback(e, root);
            if (e instanceof Object)
                throw e;
        });
    }
};
