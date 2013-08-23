package com.github.houbie.lesscss.builder;


import com.github.houbie.lesscss.Options;
import com.github.houbie.lesscss.resourcereader.FileSystemResourceReader;
import com.github.houbie.lesscss.resourcereader.ResourceReader;
import com.github.houbie.lesscss.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CompilationUnit implements Serializable {
    private static final long serialVersionUID = 3784487076551298600L;

    private File source;
    private File destination;
    private Options options;
    private ResourceReader importReader;
    private List<String> imports = new ArrayList<>();
    private String encoding;

    public CompilationUnit(File source, File destination) {
        this(source, destination, new Options());
    }

    public CompilationUnit(File source, File destination, Options options) {
        this(source, destination, options, new FileSystemResourceReader(source.getParentFile()));
    }

    public CompilationUnit(File source, File destination, Options options, ResourceReader importReader) {
        if (source == null || destination == null || options == null) {
            throw new IllegalArgumentException("source, destination and options may not be null");
        }
        this.source = source;
        this.destination = destination;
        this.options = options;
        this.importReader = importReader;
    }

    public boolean isDirty() {
        if (!destination.exists()) {
            return true;
        }
        long destinationTimestamp = destination.lastModified();
        if (source.lastModified() > destination.lastModified()) {
            return true;
        }
        if (imports != null) {
            for (String imported : imports) {
                if (importReader.lastModified(imported) > destinationTimestamp) {
                    return true;
                }
            }
        }
        return false;
    }

    public File getSource() {
        return source;
    }

    public File getDestination() {
        return destination;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public ResourceReader getImportReader() {
        return importReader;
    }

    public void setImportReader(ResourceReader importReader) {
        this.importReader = importReader;
    }

    public List<String> getImports() {
        return imports;
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getSourceAsString() throws IOException {
        if (!source.exists()) {
            throw new RuntimeException("Source does not exists: " + source);
        }
        return IOUtils.read(source, encoding);
    }

    public boolean isEquivalent(CompilationUnit other) {
        if (!destination.equals(other.destination)) return false;
        if (encoding != null ? !encoding.equals(other.encoding) : other.encoding != null) return false;
        if (importReader != null ? !importReader.equals(other.importReader) : other.importReader != null) return false;
        if (!options.equals(other.options)) return false;
        return source.equals(other.source);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompilationUnit unit = (CompilationUnit) o;
        if (!isEquivalent(unit)) {
            return false;
        }
        return (imports == null) ? unit.imports == null : imports.equals(unit.imports);
    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + destination.hashCode();
        result = 31 * result + options.hashCode();
        result = 31 * result + (importReader != null ? importReader.hashCode() : 0);
        result = 31 * result + (imports != null ? imports.hashCode() : 0);
        result = 31 * result + (encoding != null ? encoding.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompilationUnit{" +
                "source=" + source +
                ", destination=" + destination +
                '}';
    }
}
