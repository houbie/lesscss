package com.github.houbie.lesscss;


import com.github.houbie.lesscss.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class FileSystemResourceReader implements ResourceReader {
    private static Logger logger = Logger.getLogger(FileSystemResourceReader.class.getName());

    private File baseDir;
    private String encoding;

    public FileSystemResourceReader() {
        this(new File("."));
    }

    public FileSystemResourceReader(String encoding) {
        this(new File("."), encoding);
    }

    public FileSystemResourceReader(File baseDir) {
        this(baseDir, null);
    }

    public FileSystemResourceReader(File baseDir, String encoding) {
        this.baseDir = baseDir;
        this.encoding = encoding;
    }

    @Override
    public String read(String location) throws IOException {
        logger.fine("reading " + location + " relative to " + baseDir.getAbsolutePath());
        File file = new File(baseDir, location);
        if (file.canRead()) {
            return IOUtils.read(file, encoding);
        }
        return null;
    }
}
