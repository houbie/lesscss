package com.github.houbie.lesscss;


import java.io.IOException;

public interface ResourceReader {
    String read(String location) throws IOException;
}
