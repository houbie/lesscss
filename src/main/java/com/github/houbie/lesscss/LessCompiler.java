package com.github.houbie.lesscss;

import com.github.houbie.lesscss.resourcereader.ResourceReader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface LessCompiler {
    String compile(File source) throws IOException;

    String compile(File source, Options options) throws IOException;

    List<String> compile(File source, File destination) throws IOException;

    List<String> compile(File source, File destination, Options options, ResourceReader importReader, String encoding) throws IOException;

    String compile(String less);

    String compile(String less, ResourceReader importReader, String sourceName);

    String compile(String less, ResourceReader importReader, Options options, String sourceName);

    CompilationDetails compileWithDetails(String less, ResourceReader importReader, Options options, String sourceName);

    public static class CompilationDetails {
        private String result;
        private List<String> imports;

        public CompilationDetails(String result, List<String> imports) {
            this.result = result;
            this.imports = imports;
        }

        public String getResult() {
            return result;
        }

        public List<String> getImports() {
            return imports;
        }
    }
}
