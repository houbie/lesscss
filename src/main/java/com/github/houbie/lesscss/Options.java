package com.github.houbie.lesscss;


public class Options {
    private boolean compress = false;
    private int optimizationLevel = 1;
    private boolean strictImports = false;
    private String rootPath = "";
    private boolean relativeUrls = true;
    private LineNumbersOutput dumpLineNumbers = LineNumbersOutput.NONE;
    private boolean minify = false;

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
    }
}
