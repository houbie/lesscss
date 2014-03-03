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
var     parseException,
        rootFilename,
        readFileAsString,
        readFileAsBytes,
        normalize,

        javaMapToObject = function (map) {
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

        compile = function (source, options, sourceName, importReader) {
            var result,
                    lessOptions = {
                        silent: options.isSilent(),
                        lint: options.isLint(),
                        strictImports: options.isStrictImports(),
                        compress: options.isCompress(),
                        dependenciesOnly: options.isDependenciesOnly(),
                        minify: options.isMinify(),
                        ieCompat: options.isIeCompat(),
                        javascriptEnabled: options.isJavascriptEnabled(),
                        optimization: options.getOptimizationLevel(),
                        relativeUrls: options.isRelativeUrls(),
                        strictMath: options.isStrictMath(),
                        strictUnits: options.isStrictUnits(),
                        filename: sourceName
                    },
                    additionalData = {
                        globalVars: javaMapToObject(options.getGlobalVars()),
                        modifyVars: javaMapToObject(options.getModifyVars())
                    };

            rootFilename = sourceName;

            if (options.getRootpath()) {
                lessOptions.rootpath = String(options.getRootpath());
            }
            if (options.getDumpLineNumbers() && options.getDumpLineNumbers().getOptionString()) {
                lessOptions.dumpLineNumbers = String(options.getDumpLineNumbers().getOptionString());
            }

            lessOptions.currentFileInfo = {
                relativeUrls: lessOptions.relativeUrls,  //option - whether to adjust URL's to be relative
                filename: sourceName,                    //full resolved filename of current file
                rootpath: lessOptions.rootpath,          //path to append to normal URLs for this node
                currentDirectory: '',                    //path to the current file, absolute
                rootFilename: rootFilename,              //filename of the base file
                entryPath: ''                            //absolute path to the entry file
            };

            readFileAsString = function (file) {
                var data = importReader.read(file);
                if (data === null) {
                    throw {type: 'File', message: "'" + file + "' wasn't found"};
                }
                return String(data);
            };

            readFileAsBytes = function (file) {
                var data = importReader.readBytes(file);
                if (data === null) {
                    throw {type: 'File', message: "'" + file + "' wasn't found"};
                }
                return data;
            };

            normalize = function (path) {
                return String(importReader.normalize(path));
            };

            try {
                parseException = null;
                new (less.Parser)(lessOptions).parse(String(source), function (e, tree) {
                    if (e) {
                        throw e;
                    }
                    result = (lessOptions.dependenciesOnly) ? '' : tree.toCSS(lessOptions);
                }, additionalData);
                return (options.minify) ? cssmin(result) : result;
            } catch (e) {
                parseException = 'less parse exception: ' + e.message;
                if (e.filename) {
                    parseException += '\nin ' + e.filename + ' at line ' + e.line;
                }
                if (e.extract) {
                    var extract = e.extract;
                    parseException += '\nextract';
                    for (var line in extract) {
                        if (extract.hasOwnProperty(line) && extract[line]) {
                            parseException += '\n' + e.extract[line];
                        }
                    }
                }
                return null;
            }
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
        data = readFileAsString(href);
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
