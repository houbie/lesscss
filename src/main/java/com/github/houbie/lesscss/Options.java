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

package com.github.houbie.lesscss;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ivo Houbrechts
 */
public class Options implements Serializable {
    private boolean compress = false;
    private int optimizationLevel = 1;
    private boolean strictImports = false;
    private String rootpath;
    private boolean relativeUrls = false;
    private LineNumbersOutput dumpLineNumbers = LineNumbersOutput.NONE;
    private boolean minify = false;
    private boolean dependenciesOnly = false;
    private boolean strictMath = false;
    private boolean strictUnits = false;
    private boolean ieCompat = true;
    private boolean javascriptEnabled = true;
    private boolean lint = false;
    private boolean silent = false;
    private boolean sourceMap = false;
    private String sourceMapRootpath;
    private String sourceMapBasepath;
    private boolean sourceMapLessInline;
    private boolean sourceMapMapInline;
    private String sourceMapUrl;
    private Map<String, String> globalVars = new HashMap<String, String>();
    private Map<String, String> modifyVars = new HashMap<String, String>();

    public Options() {
    }

    public Options(Options other) {
        this.compress = other.compress;
        this.optimizationLevel = other.optimizationLevel;
        this.strictImports = other.strictImports;
        this.rootpath = other.rootpath;
        this.relativeUrls = other.relativeUrls;
        this.dumpLineNumbers = other.dumpLineNumbers;
        this.minify = other.minify;
        this.dependenciesOnly = other.dependenciesOnly;
        this.strictMath = other.strictMath;
        this.strictUnits = other.strictUnits;
        this.ieCompat = other.ieCompat;
        this.javascriptEnabled = other.javascriptEnabled;
        this.lint = other.lint;
        this.silent = other.silent;
        this.sourceMap = other.sourceMap;
        this.sourceMapRootpath = other.sourceMapRootpath;
        this.sourceMapBasepath = other.sourceMapBasepath;
        this.sourceMapLessInline = other.sourceMapLessInline;
        this.sourceMapMapInline = other.sourceMapMapInline;
        this.sourceMapUrl = other.sourceMapUrl;
        this.globalVars = other.globalVars;
        this.modifyVars = other.modifyVars;
    }

    /**
     * Compress output by removing some whitespaces (default: false)
     *
     * @return compress option
     */
    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    /**
     * Optimization levels: The lower the number, the fewer nodes created in the tree. Useful for debugging or if you need to access the individual nodes in the tree. (default: 1)
     *
     * @return optimization level
     */
    public int getOptimizationLevel() {
        return optimizationLevel;
    }

    public void setOptimizationLevel(int optimizationLevel) {
        this.optimizationLevel = optimizationLevel;
    }

    /**
     * Force evaluation of imports (default: false)
     *
     * @return strict imports option
     */
    public boolean isStrictImports() {
        return strictImports;
    }

    public void setStrictImports(boolean strictImports) {
        this.strictImports = strictImports;
    }

    /**
     * Rootpath for URL rewriting in relative imports and URLs (default: empty string)
     *
     * @return rootpath
     */
    public String getRootpath() {
        return rootpath;
    }

    public void setRootpath(String rootpath) {
        this.rootpath = rootpath;
    }

    /**
     * Re-write relative URLs to the base less file (default: true)
     *
     * @return relative urls option
     */
    public boolean isRelativeUrls() {
        return relativeUrls;
    }

    public void setRelativeUrls(boolean relativeUrls) {
        this.relativeUrls = relativeUrls;
    }

    /**
     * If and where to output line number information (default: none)
     *
     * @return dumpLineNumbers option
     */
    public LineNumbersOutput getDumpLineNumbers() {
        return dumpLineNumbers;
    }

    public void setDumpLineNumbers(LineNumbersOutput dumpLineNumbers) {
        if (dumpLineNumbers == null) {
            throw new NullPointerException("Options.dumpLineNumbers may not be null");
        }
        this.dumpLineNumbers = dumpLineNumbers;
    }

    /**
     * Minify output using YUI cssmin (default: false)
     *
     * @return minify option
     */
    public boolean isMinify() {
        return minify;
    }

    public void setMinify(boolean minify) {
        this.minify = minify;
    }

    /**
     * If true, only imports statements are evaluated without actual compilation (default: false)
     *
     * @return true if only imports are evaluated
     */
    public boolean isDependenciesOnly() {
        return dependenciesOnly;
    }

    public void setDependenciesOnly(boolean dependenciesOnly) {
        this.dependenciesOnly = dependenciesOnly;
    }


    /**
     * When in strict math mode, math requires brackets (default: false)
     *
     * @return true if strict-math option is on
     */
    public boolean isStrictMath() {
        return strictMath;
    }

    public void setStrictMath(boolean strictMath) {
        this.strictMath = strictMath;
    }

    /**
     * When in strict units mode, mixed units, e.g. 1px+1em or 1px*1px which have units that cannot be represented,
     * are not allowed  (default: false)
     *
     * @return true if strict-units option is on
     */
    public boolean isStrictUnits() {
        return strictUnits;
    }

    public void setStrictUnits(boolean strictUnits) {
        this.strictUnits = strictUnits;
    }

    /**
     * Enable IE compatibility checks (default: true)
     *
     * @return true if no-ie-compat option is off
     */
    public boolean isIeCompat() {
        return ieCompat;
    }

    public void setIeCompat(boolean ieCompat) {
        this.ieCompat = ieCompat;
    }

    /**
     * Enable JavaScript in less files (default: true)
     *
     * @return true if no-js option is off
     */
    public boolean isJavascriptEnabled() {
        return javascriptEnabled;
    }

    public void setJavascriptEnabled(boolean javascriptEnabled) {
        this.javascriptEnabled = javascriptEnabled;
    }

    /**
     * Syntax check only (lint) (default: false)
     *
     * @return true if lint option is on
     */
    public boolean isLint() {
        return lint;
    }

    public void setLint(boolean lint) {
        this.lint = lint;
    }

    /**
     * Suppress output of error messages (default: false)
     *
     * @return true if silent option is on
     */
    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    /**
     * Outputs a v3 sourcemap to the filename (or output filename.map) (default: false)
     *
     * @return true if source-map option is on
     */
    public boolean isSourceMap() {
        return sourceMap;
    }

    public void setSourceMap(boolean sourceMap) {
        this.sourceMap = sourceMap;
    }

    /**
     * The path that is added onto the sourc emap filename and less file paths (default: empty)
     *
     * @return source map root path
     */
    public String getSourceMapRootpath() {
        return sourceMapRootpath;
    }

    public void setSourceMapRootpath(String sourceMapRootpath) {
        this.sourceMapRootpath = sourceMapRootpath;
    }

    /**
     * The source map base path (default: current working directory)
     *
     * @return source map base path
     */
    public String getSourceMapBasepath() {
        return sourceMapBasepath;
    }

    public void setSourceMapBasepath(String sourceMapBasepath) {
        this.sourceMapBasepath = sourceMapBasepath;
    }

    /**
     * Whether to put the less files into the map instead of referencing them (default: false)
     *
     * @return true if less files are in-lined
     */
    public boolean isSourceMapLessInline() {
        return sourceMapLessInline;
    }

    public void setSourceMapLessInline(boolean sourceMapLessInline) {
        this.sourceMapLessInline = sourceMapLessInline;
    }

    /**
     * Whether to put the source map (and any less files) into the output css file (default: false)
     *
     * @return true if the maps are inlined
     */
    public boolean isSourceMapMapInline() {
        return sourceMapMapInline;
    }

    public void setSourceMapMapInline(boolean sourceMapMapInline) {
        this.sourceMapMapInline = sourceMapMapInline;
    }

    /**
     * The complete url and filename put in the less file (default: empty)
     *
     * @return the source map url
     */
    public String getSourceMapUrl() {
        return sourceMapUrl;
    }

    public void setSourceMapUrl(String sourceMapUrl) {
        this.sourceMapUrl = sourceMapUrl;
    }

    /**
     * Map of global variables that can be referenced in less (default: empty)
     *
     * @return global variables
     */
    public Map<String, String> getGlobalVars() {
        return globalVars;
    }

    public void setGlobalVars(Map<String, String> globalVars) {
        this.globalVars = globalVars;
    }

    /**
     * Map of variables that will overwrite variables already in the less (default: empty)
     *
     * @return modified variables to overwrite
     */
    public Map<String, String> getModifyVars() {
        return modifyVars;
    }

    public void setModifyVars(Map<String, String> modifyVars) {
        this.modifyVars = modifyVars;
    }

    public enum LineNumbersOutput {
        /**
         * No line number output
         */
        NONE(null),
        /**
         * Output line numbers in CSS comments
         */
        COMMENTS("comments"),
        /**
         * Output line numbers within a fake media query which is compatible with the SASS format
         */
        MEDIA_QUERY("mediaquery"),
        /**
         * Output line numbers in both comments and media query
         */
        ALL("all");

        private final String optionString;

        LineNumbersOutput(String optionString) {
            this.optionString = optionString;
        }

        public String getOptionString() {
            return optionString;
        }

        public static LineNumbersOutput fromOptionString(String optionString) {
            if (optionString == null || optionString.trim().length() == 0) {
                return NONE;
            }
            if (COMMENTS.getOptionString().equals(optionString)) {
                return COMMENTS;
            }
            if (MEDIA_QUERY.getOptionString().equals(optionString)) {
                return MEDIA_QUERY;
            }
            if (ALL.getOptionString().equals(optionString)) {
                return ALL;
            }
            throw new RuntimeException("Invalid line numbers type: " + optionString);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Options options = (Options) o;

        if (minify != options.minify) return false;
        if (compress != options.compress) return false;
        if (dependenciesOnly != options.dependenciesOnly) return false;
        if (ieCompat != options.ieCompat) return false;
        if (javascriptEnabled != options.javascriptEnabled) return false;
        if (lint != options.lint) return false;
        if (optimizationLevel != options.optimizationLevel) return false;
        if (relativeUrls != options.relativeUrls) return false;
        if (silent != options.silent) return false;
        if (sourceMap != options.sourceMap) return false;
        if (sourceMapLessInline != options.sourceMapLessInline) return false;
        if (sourceMapMapInline != options.sourceMapMapInline) return false;
        if (strictImports != options.strictImports) return false;
        if (strictMath != options.strictMath) return false;
        if (strictUnits != options.strictUnits) return false;
        if (dumpLineNumbers != options.dumpLineNumbers) return false;
        if (!globalVars.equals(options.globalVars)) return false;
        if (!modifyVars.equals(options.modifyVars)) return false;
        if (rootpath != null ? !rootpath.equals(options.rootpath) : options.rootpath != null) return false;
        if (sourceMapBasepath != null ? !sourceMapBasepath.equals(options.sourceMapBasepath) : options.sourceMapBasepath != null)
            return false;
        if (sourceMapRootpath != null ? !sourceMapRootpath.equals(options.sourceMapRootpath) : options.sourceMapRootpath != null)
            return false;
        if (sourceMapUrl != null ? !sourceMapUrl.equals(options.sourceMapUrl) : options.sourceMapUrl != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (compress ? 1 : 0);
        result = 31 * result + optimizationLevel;
        result = 31 * result + (strictImports ? 1 : 0);
        result = 31 * result + (rootpath != null ? rootpath.hashCode() : 0);
        result = 31 * result + (relativeUrls ? 1 : 0);
        result = 31 * result + dumpLineNumbers.hashCode();
        result = 31 * result + (minify ? 1 : 0);
        result = 31 * result + (dependenciesOnly ? 1 : 0);
        result = 31 * result + (strictMath ? 1 : 0);
        result = 31 * result + (strictUnits ? 1 : 0);
        result = 31 * result + (ieCompat ? 1 : 0);
        result = 31 * result + (javascriptEnabled ? 1 : 0);
        result = 31 * result + (lint ? 1 : 0);
        result = 31 * result + (silent ? 1 : 0);
        result = 31 * result + (sourceMap ? 1 : 0);
        result = 31 * result + (sourceMapRootpath != null ? sourceMapRootpath.hashCode() : 0);
        result = 31 * result + (sourceMapBasepath != null ? sourceMapBasepath.hashCode() : 0);
        result = 31 * result + (sourceMapLessInline ? 1 : 0);
        result = 31 * result + (sourceMapMapInline ? 1 : 0);
        result = 31 * result + (sourceMapUrl != null ? sourceMapUrl.hashCode() : 0);
        result = 31 * result + globalVars.hashCode();
        result = 31 * result + modifyVars.hashCode();
        return result;
    }
}
