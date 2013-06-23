package com.github.houbie.lesscss;


import com.github.houbie.lesscss.utils.IOUtils;

import java.io.File;
import java.io.IOException;
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
        for (File dir : baseDirs) {
            logger.fine("reading " + location + " relative to " + dir.getAbsolutePath());
            File file = new File(dir, location);
            if (file.canRead()) {
                return IOUtils.read(file, encoding);
            }
        }
        return null;
    }

    public File[] getBaseDirs() {
        return baseDirs;
    }

    public String getEncoding() {
        return encoding;
    }
}
