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

public class Options implements Serializable {
    private static final long serialVersionUID = -4196621429522278886L;

    private boolean compress = false;
    private int optimizationLevel = 1;
    private boolean strictImports = false;
    private String rootPath = "";
    private boolean relativeUrls = true;
    private LineNumbersOutput dumpLineNumbers = LineNumbersOutput.NONE;
    private boolean minify = false;
    private boolean dependenciesOnly = false;

    public Options() {
    }

    public Options(Options other) {
        this.compress = other.compress;
        this.optimizationLevel = other.optimizationLevel;
        this.strictImports = other.strictImports;
        this.rootPath = other.rootPath;
        this.relativeUrls = other.relativeUrls;
        this.dumpLineNumbers = other.dumpLineNumbers;
        this.minify = other.minify;
        this.dependenciesOnly = other.dependenciesOnly;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public int getOptimizationLevel() {
        return optimizationLevel;
    }

    public void setOptimizationLevel(int optimizationLevel) {
        this.optimizationLevel = optimizationLevel;
    }

    public boolean isStrictImports() {
        return strictImports;
    }

    public void setStrictImports(boolean strictImports) {
        this.strictImports = strictImports;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        if (rootPath == null) {
            throw new NullPointerException("Options.rootPath may not be null");
        }
        this.rootPath = rootPath;
    }

    public boolean isRelativeUrls() {
        return relativeUrls;
    }

    public void setRelativeUrls(boolean relativeUrls) {
        this.relativeUrls = relativeUrls;
    }

    public LineNumbersOutput getDumpLineNumbers() {
        return dumpLineNumbers;
    }

    public void setDumpLineNumbers(LineNumbersOutput dumpLineNumbers) {
        if (dumpLineNumbers == null) {
            throw new NullPointerException("Options.dumpLineNumbers may not be null");
        }
        this.dumpLineNumbers = dumpLineNumbers;
    }

    public boolean isMinify() {
        return minify;
    }

    public void setMinify(boolean minify) {
        this.minify = minify;
    }

    public boolean isDependenciesOnly() {
        return dependenciesOnly;
    }

    public void setDependenciesOnly(boolean dependenciesOnly) {
        this.dependenciesOnly = dependenciesOnly;
    }

    public enum LineNumbersOutput {
        NONE(null),
        COMMENTS("comments"),
        MEDIA_QUERY("mediaquery"),
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

        if (compress != options.compress) return false;
        if (dependenciesOnly != options.dependenciesOnly) return false;
        if (minify != options.minify) return false;
        if (optimizationLevel != options.optimizationLevel) return false;
        if (relativeUrls != options.relativeUrls) return false;
        if (strictImports != options.strictImports) return false;
        if (dumpLineNumbers != options.dumpLineNumbers) return false;
        if (rootPath != null ? !rootPath.equals(options.rootPath) : options.rootPath != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (compress ? 1 : 0);
        result = 31 * result + optimizationLevel;
        result = 31 * result + (strictImports ? 1 : 0);
        result = 31 * result + (rootPath != null ? rootPath.hashCode() : 0);
        result = 31 * result + (relativeUrls ? 1 : 0);
        result = 31 * result + (dumpLineNumbers != null ? dumpLineNumbers.hashCode() : 0);
        result = 31 * result + (minify ? 1 : 0);
        result = 31 * result + (dependenciesOnly ? 1 : 0);
        return result;
    }
}
