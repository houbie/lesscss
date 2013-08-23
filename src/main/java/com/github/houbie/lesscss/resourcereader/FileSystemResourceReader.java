package com.github.houbie.lesscss.resourcereader;


import com.github.houbie.lesscss.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

public class FileSystemResourceReader implements ResourceReader {
    private static Logger logger = Logger.getLogger(FileSystemResourceReader.class.getName());

    private File[] baseDirs;
    private String encoding;

    public FileSystemResourceReader() {
        this(new File("."));
    }

    public FileSystemResourceReader(String encoding) {
        this(encoding, new File("."));
    }

    public FileSystemResourceReader(File... baseDirs) {
        this(null, baseDirs);
    }

    public FileSystemResourceReader(String encoding, File... baseDirs) {
        this.baseDirs = baseDirs;
        this.encoding = encoding;
    }

    @Override
    public String read(String location) throws IOException {
        File file = resolve(location);
        return (file != null) ? IOUtils.read(file, encoding) : null;
    }

    private File resolve(String location) {
        File file = resolveRelative(location);
        if (file == null) {
            file = resolveAbsolute(location);
        }
        return file;
    }

    private File resolveRelative(String location) {
        for (File dir : baseDirs) {
            logger.fine("reading " + location + " relative to " + dir.getAbsolutePath());
            File file = new File(dir, location);
            if (file.canRead()) {
                return file;
            }
        }
        return null;
    }

    private File resolveAbsolute(String location) {
        File file = new File(location);
        return (file.canRead()) ? file : null;
    }

    @Override
    public long lastModified(String location) {
        File file = resolve(location);
        return (file == null) ? Long.MAX_VALUE : file.lastModified();
    }

    public File[] getBaseDirs() {
        return baseDirs;
    }

    public String getEncoding() {
        return encoding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileSystemResourceReader that = (FileSystemResourceReader) o;

        if (!Arrays.equals(baseDirs, that.baseDirs)) return false;
        if (encoding != null ? !encoding.equals(that.encoding) : that.encoding != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = baseDirs != null ? Arrays.hashCode(baseDirs) : 0;
        result = 31 * result + (encoding != null ? encoding.hashCode() : 0);
        return result;
    }
}
