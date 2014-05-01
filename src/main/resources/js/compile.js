/*
 * Copyright (c) 2013 Houbrechts IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * Compile function to be called from Java
 */
var javaMapToObject = function (map) {
            var iter = map.keySet().iterator(),
                    key,
                    result;
            if (iter.hasNext()) {
                result = {};
                while (iter.hasNext()) {
                    key = iter.next();
                    result[String(key)] = String(map.get(key));
                }
                return result;
            }
            return null;
        },

        setSourceMapOptions = function (lessOptions, compilationOptions, result) {
            lessOptions.sourceMapOutputFilename = String(compilationOptions.destinationFilename);
            if (compilationOptions.options.sourceMapMapInline) {
                lessOptions.sourceMap = true;
            }
            if (compilationOptions.options.sourceMapRootpath) {
                lessOptions.sourceMapRootpath = String(compilationOptions.options.sourceMapRootpath);
            }
            if (compilationOptions.options.sourceMapBasepath) {
                lessOptions.sourceMapBasepath = String(compilationOptions.options.sourceMapBasepath);
            } else {
                lessOptions.sourceMapBasepath = less.modules.path.dirname(String(compilationOptions.sourceFilename));
            }
            if (compilationOptions.options.sourceMapURL) {
                lessOptions.sourceMapURL = String(compilationOptions.options.sourceMapURL);
            }
            if (compilationOptions.options.sourceMapLessInline) {
                lessOptions.outputSourceFiles = true;
            }
            if (compilationOptions.options.sourceMap) {
                lessOptions.sourceMapFullFilename = String(compilationOptions.sourceMapFilename);
                lessOptions.sourceMapFilename = lessOptions.sourceMapFullFilename;
                lessOptions.sourceMap = less.modules.path.basename(lessOptions.sourceMapFullFilename);
                lessOptions.writeSourceMap = function (content) {
                    result.sourceMapContent = content;
                };
            }
        },

        compile = function (source, compilationOptions, importReader) {
            var result = {
                        css: null,
                        sourceMapContent: null,
                        parseException: null
                    },

                    sourceFileName = String(compilationOptions.sourceFilename),
                    sourceDir = less.modules.path.dirname(sourceFileName),

                    lessOptions = {
                        silent: compilationOptions.options.silent,
                        lint: compilationOptions.options.lint,
                        strictImports: compilationOptions.options.strictImports,
                        compress: compilationOptions.options.compress,
                        dependenciesOnly: compilationOptions.options.dependenciesOnly,
                        minify: compilationOptions.options.minify,
                        ieCompat: compilationOptions.options.ieCompat,
                        javascriptEnabled: compilationOptions.options.javascriptEnabled,
                        optimization: compilationOptions.options.optimizationLevel,
                        relativeUrls: compilationOptions.options.relativeUrls,
                        strictMath: compilationOptions.options.strictMath,
                        strictUnits: compilationOptions.options.strictUnits,
                        filename: less.modules.path.basename(sourceFileName)
                    },
                    additionalData = {
                        globalVars: javaMapToObject(compilationOptions.options.globalVars),
                        modifyVars: javaMapToObject(compilationOptions.options.modifyVars)
                    };

            if (compilationOptions.options.rootpath) {
                lessOptions.rootpath = String(compilationOptions.options.rootpath);
            }
            if (compilationOptions.options.dumpLineNumbers && compilationOptions.options.dumpLineNumbers.optionString) {
                lessOptions.dumpLineNumbers = String(compilationOptions.options.dumpLineNumbers.optionString);
            }
            setSourceMapOptions(lessOptions, compilationOptions, result);

            lessOptions.currentFileInfo = {
                relativeUrls: lessOptions.relativeUrls, //option - whether to adjust URL's to be relative
                filename: String(compilationOptions.sourceFilename), //full resolved filename of current file
                rootpath: lessOptions.rootpath, //path to append to normal URLs for this node
                currentDirectory: '', //path to the current file, absolute
                rootFilename: lessOptions.filename, //filename of the base file
                entryPath: sourceFileName, //absolute path to the entry file

                readFileAsString: function (file) {
                    var data = importReader.read(file) || importReader.read(less.modules.path.join(sourceDir, file));
                    if (data == null) {
                        throw {type: 'File', message: "'" + file + "' wasn't found"};
                    }
                    return String(data);
                },

                readFileAsBytes: function (file) {
                    var data = importReader.readBytes(file) || importReader.readBytes(less.modules.path.join(sourceDir, file));
                    if (data === null) {
                        throw {type: 'File', message: "'" + file + "' wasn't found"};
                    }
                    return data;
                }
            };

            try {
                new (less.Parser)(lessOptions).parse(String(source), function (e, tree) {
                    if (e) {
                        throw e;
                    }
                    result.css = (lessOptions.dependenciesOnly) ? '' : tree.toCSS(lessOptions);
                }, additionalData);
                if (lessOptions.minify) {
                    result.css = cssmin(result.css);
                }
            } catch (e) {
                result.parseException = 'less parse exception: ' + e.message;
                if (e.filename) {
                    result.parseException += '\nin ' + e.filename + ' at line ' + e.line;
                }
                if (e.extract) {
                    var extract = e.extract;
                    result.parseException += '\nextract';
                    for (var line in extract) {
                        if (extract.hasOwnProperty(line) && extract[line]) {
                            result.parseException += '\n' + e.extract[line];
                        }
                    }
                }
            }
            return result;
        };

less.Parser.fileLoader = function (file, currentFileInfo, callback, env) {

    var href = file;
    if (currentFileInfo && currentFileInfo.currentDirectory && !/^\//.test(file)) {
        href = less.modules.path.join(currentFileInfo.currentDirectory, file);
    }

    var path = less.modules.path.dirname(href);

    var newFileInfo = {
        currentDirectory: path + '/',
        filename: href
    };

    if (currentFileInfo) {
        newFileInfo.entryPath = currentFileInfo.entryPath;
        newFileInfo.rootpath = currentFileInfo.rootpath;
        newFileInfo.rootFilename = currentFileInfo.rootFilename;
        newFileInfo.relativeUrls = currentFileInfo.relativeUrls;
        newFileInfo.readFileAsString = currentFileInfo.readFileAsString;
        newFileInfo.readFileAsBytes = currentFileInfo.readFileAsBytes;
    } else {
        newFileInfo.entryPath = path;
        newFileInfo.rootpath = less.rootpath || path;
        newFileInfo.rootFilename = href;
        newFileInfo.relativeUrls = env.relativeUrls;
    }

    var j = file.lastIndexOf('/');
    if (newFileInfo.relativeUrls && !/^(?:[a-z-]+:|\/)/.test(file) && j != -1) {
        var relativeSubDirectory = file.slice(0, j + 1);
        newFileInfo.rootpath = (newFileInfo.rootpath || '') + relativeSubDirectory; // append (sub|sup) directory path of imported file
    }
    newFileInfo.currentDirectory = path;
    newFileInfo.filename = href;

    var data = null;
    try {
        data = currentFileInfo.readFileAsString(href);
    } catch (e) {
        callback({ type: 'File', message: "'" + less.modules.path.basename(href) + "' wasn't found" });
        return;
    }

    try {
        callback(null, data, href, newFileInfo, { lastModified: 0 });
    } catch (e) {
        callback(e, null, href);
    }
};
