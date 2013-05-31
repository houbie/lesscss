package com.github.houbie.lesscss.utils;

import java.io.*;
import java.net.URL;

public class IOUtils {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    public static String read(URL url) throws IOException {
        return read(url, null);
    }

    public static String read(URL url, String encoding) throws IOException {
        InputStream is = null;
        StringBuilderWriter writer = new StringBuilderWriter();
        try {
            is = url.openStream();
            copy(is, writer, encoding);
            return writer.toString();
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public static String read(File file) throws IOException {
        return read(file, null);
    }

    public static String read(File file, String encoding) throws IOException {
        InputStream is = null;
        StringBuilderWriter writer = new StringBuilderWriter();
        try {
            is = new FileInputStream(file);
            copy(is, writer, encoding);
            return writer.toString();
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public static void copy(InputStream input, Writer output, String encoding)
            throws IOException {
        if (encoding == null) {
            copy(input, output);
        } else {
            InputStreamReader in = new InputStreamReader(input, encoding);
            copy(in, output);
        }
    }

    public static void copy(InputStream input, Writer output)
            throws IOException {
        InputStreamReader in = new InputStreamReader(input);
        copy(in, output);
    }

    public static int copy(java.io.Reader input, Writer output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(Reader input, Writer output) throws IOException {
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

}
