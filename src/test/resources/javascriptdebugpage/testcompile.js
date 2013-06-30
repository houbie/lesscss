//compile = function (source, optionsArg, sourceName, importReaderArg) {

var test = (function () {
    var options = {
                rootPath: '',
                dumpLineNumbers: {getOptionString: function () {
                    return null;
                }}},
            source = '#import {\n' +
                    '  color: red;\n' +
                    '}\n' +
                    '\n' +
                    '@import "import1/imported1";\n' +
                    '@import "imported0";',
            sourceName = 'import.less',
            imports = {
                'import1/commonImported.less': '#commonImported1 {\n' +
                        '  color: green;\n' +
                        '}',
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
                        '}}\n' +
                        '\n' +
                        '@import "../commonImported";\n' +
                        '@import "commonImported";'
            },

            read = function (fullPath) {
                console.log('read ' + fullPath);
                return imports[fullPath];
            },

            doit = function () {
                var result = compile(source, options, sourceName, test);
                console.error('error', parseException);
                console.log('result', result);
            };

    return {
        read: read,
        compile: compile,
        doit: doit
    }
}());
