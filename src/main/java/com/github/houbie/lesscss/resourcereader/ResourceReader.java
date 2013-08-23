package com.github.houbie.lesscss.resourcereader;


import java.io.IOException;
import java.io.Serializable;

public interface ResourceReader extends Serializable {
    String read(String location) throws IOException;

    long lastModified(String location);
}
