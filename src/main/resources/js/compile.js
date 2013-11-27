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
var less = window.less,
        parseException,
        rootFilename,
        readFile,
        normalize,

        compile = function (source, options, sourceName, importReader) {
            var result,
                    rootPath = String(options.getRootPath()),
                    lessEnv = {
                        compress: options.isCompress(),
                        optimization: options.getOptimizationLevel(),
                        strictImports: options.isStrictImports(),
                        strictMath: options.isStrictMath(),
                        strictUnits: options.isStrictUnits(),
                        relativeUrls: options.isRelativeUrls(),
                        filename: sourceName,
                        dependenciesOnly: options.isDependenciesOnly()
                    };

            rootFilename = sourceName;

            if (rootPath.length > 0) {
                lessEnv.rootpath = rootPath;
            }
            if (options.getDumpLineNumbers() && options.getDumpLineNumbers().getOptionString()) {
                lessEnv.dumpLineNumbers = String(options.getDumpLineNumbers().getOptionString());
            }

            lessEnv.currentFileInfo = {
                relativeUrls: lessEnv.relativeUrls,  //option - whether to adjust URL's to be relative
                filename: sourceName,                //full resolved filename of current file
                rootpath: rootPath,                  //path to append to normal URLs for this node
                currentDirectory: '',                //path to the current file, absolute
                rootFilename: rootFilename,          //filename of the base file
                entryPath: ''                        //absolute path to the entry file
            };

            readFile = function (file) {
                var data = importReader.read(file);
                if (data === null) {
                    throw {type: 'File', message: "'" + file + "' wasn't found"};
                }
                return String(data);
            };

            normalize = function (path) {
                return String(importReader.normalize(path));
            };

            try {
                parseException = null;
                new (less.Parser)(lessEnv).parse(String(source), function (e, tree) {
                    if (e) {
                        throw e;
                    }
                    result = (lessEnv.dependenciesOnly) ? '' : tree.toCSS(lessEnv);
                });
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

less.Parser.importer = function (file, currentFileInfo, callback, env) {
    var pathname = normalize(currentFileInfo.currentDirectory + file),
            data,
            newFileInfo = {
                relativeUrls: env.relativeUrls,
                entryPath: currentFileInfo.entryPath,
                rootpath: currentFileInfo.rootpath,
                rootFilename: currentFileInfo.rootFilename
            },

            parseFile = function (e) {
                if (e) {
                    callback(e);
                    return;
                }

                env = new less.tree.parseEnv(env);
                env.processImports = false;

                var j = file.lastIndexOf('/');

                // Pass on an updated rootpath if path of imported file is relative and file
                // is in a (sub|sup) directory
                //
                // Examples:
                // - If path of imported file is 'module/nav/nav.less' and rootpath is 'less/',
                //   then rootpath should become 'less/module/nav/'
                // - If path of imported file is '../mixins.less' and rootpath is 'less/',
                //   then rootpath should become 'less/../'
                if (newFileInfo.relativeUrls && !/^(?:[a-z-]+:|\/)/.test(file) && j != -1) {
                    var relativeSubDirectory = file.slice(0, j + 1);
                    newFileInfo.rootpath = newFileInfo.rootpath + relativeSubDirectory; // append (sub|sup) directory path of imported file
                }
                newFileInfo.currentDirectory = pathname.replace(/[^\\\/]*$/, "");
                newFileInfo.filename = pathname;

                env.contents[pathname] = data;      // Updating top importing parser content cache.
                env.currentFileInfo = newFileInfo;
                new (less.Parser)(env).parse(data, function (e, root) {
                    callback(e, root, pathname);
                });
            };

    try {
        data = readFile(pathname);
        parseFile(null);
    } catch (e) {
        parseFile(e);
    }
};
