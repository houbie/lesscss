package com.github.houbie.lesscss.resourcereader;

import com.github.houbie.lesscss.LessCompilerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ImportCapturingResourceReader implements ResourceReader {
    private static final Logger logger = LoggerFactory.getLogger(LessCompilerImpl.class);

    private ResourceReader resourceReader;

    private List<String> imports = new ArrayList<>();

    public ImportCapturingResourceReader(ResourceReader resourceReader) {
        this.resourceReader = resourceReader;
    }

    public String read(String location) throws IOException {
        logger.debug("reading @import " + location);
        if (resourceReader == null) {
            throw new RuntimeException("Error in less compilation: import of " + location + " failed because no ResourceReader is configured");
        }
        try {
            //resolve ./ and ../
            imports.add(new URI(location).normalize().getPath());
        } catch (URISyntaxException e) {
            logger.warn("exeption while normalizing import url: " + e.getMessage());
            imports.add(location);
        }
        return resourceReader.read(location);
    }

    @Override
    public long lastModified(String location) {
        return resourceReader.lastModified(location);
    }

    public List<String> getImports() {
        return imports;
    }
}
