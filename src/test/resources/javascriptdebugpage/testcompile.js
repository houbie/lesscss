//compile = function (source, optionsArg, sourceName, importReaderArg)

var test = (function () {
    var options = {
                getRootpath: function () {
                    return '';
                },
                isCompress: function () {
                    return false;
                },
                getOptimizationLevel: function () {
                    return 1;
                },
                isStrictMath: function () {
                    return false;
                },
                isStrictUnits: function () {
                    return false;
                },
                isStrictImports: function () {
                    return false;
                },
                isDependenciesOnly: function () {
                    return false;
                },
                isRelativeUrls: function () {
                    return true;
                },
                getDumpLineNumbers: function () {
                    return {
                        getOptionString: function () {
                            return null;
                        }
                    }
                }
            },
            sourceTestImports = '#import {\n' +
                    '  color: red;\n' +
                    '}\n' +
                    '\n' +
                    '@import "import1/imported1";\n' +
                    '@import "imported0";',
            sourceNameTestImports = 'import.less',
            sourceTestUrls = '#data-uri {\n' +
                    '  uri: data-uri(\'image/jpeg;base64\', \'../less.js-tests/data/image.jpg\');\n' +
                    '}\n',
            sourceNameTestUrls = 'import.less',
            imports = {
                'import1/import2/../commonImported.less': '#commonImported1 {\n' +
                        '  color: green;\n' +
                        '}',
                'import1/commonImported.less': '#commonImported1 {\n' +
                        '  color: green;\n' +
                        '}',
                'imported0.less': '#imported0 {\n' +
                        'color: orange;\n' +
                        '}\n',
                'import1/imported1.less': '@import "../css/background.css";\n' +
                        '\n' +
                        '#imported1 {\n' +
                        '  color: blue;\n' +
                        '  background: url(\'assets/logo.png\');\n' +
                        '}\n' +
                        '\n' +
                        '@import "import2/imported2";',
                'import1/import2/commonImported.less': '#commonInImported2 {\n' +
                        '  color: purple;\n' +
                        '}',
                'import1/import2/imported2.less': '#imported2 {\n' +
                        '  color: black;\n' +
                        '}\n' +
                        '\n' +
                        '@import "../commonImported";\n' +
                        '@import "commonImported";',
                'import/import-and-relative-paths-test': '#dummy {}'
            },

            read = function (fullPath) {
                console.log('read ' + fullPath);
                var result = imports[fullPath];
                if (!result || !result.length) {
                    console.error('could not find import', fullPath);
                    return null;
                }
                return result;
            },

            normalize = function (path) {
                if (path === 'import1/import2/../commonImported.less') {
                    return 'import1/commonImported.less';
                }
                return path;
            },

            testImports = function () {
                var result = compile(sourceTestImports, options, sourceNameTestImports, test);
                console.error('error', parseException);
                console.log('result', result);
            },

            testUrls = function () {
                var result = compile(sourceTestUrls, options, sourceNameTestUrls, test);
                console.error('error', parseException);
                console.log('result', result);
            };

    return {
        read: read,
        compile: compile,
        normalize: normalize,
        testImports: testImports,
        testUrls: testUrls
    }
}());
