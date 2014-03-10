/*
 * Copyright (c) 2013 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.houbie.lesscss.utils;

import java.io.*;
import java.net.URL;

/**
 * Utility class to read/write files and streams.
 * Most of the code is copied from Apache Commons IO
 *
 * @author Ivo Houbrechts
 */
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

    public static String read(InputStream inputStream) throws IOException {
        StringBuilderWriter writer = new StringBuilderWriter();
        copyLarge(new InputStreamReader(inputStream), writer);
        return writer.toString();
    }

    public static String read(Reader reader) throws IOException {
        StringBuilderWriter writer = new StringBuilderWriter();
        copyLarge(reader, writer);
        return writer.toString();
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

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;

        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }


    public static long copyLarge(Reader input, Writer output) throws IOException {
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void writeFile(String content, File destination, String charsetName) throws IOException {
        if (!destination.exists()) {
            destination.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(destination);
        write(content, fos, charsetName);
    }

    public static void write(String content, OutputStream outputStream, String charsetName) throws IOException {
        OutputStreamWriter writer = (charsetName != null) ? new OutputStreamWriter(outputStream, charsetName) : new OutputStreamWriter(outputStream);
        copyLarge(new StringReader(content), writer);
        writer.flush();
        writer.close();
    }
}
