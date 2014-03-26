/*
 * Copyright (c) 2013 Houbrechts IT
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

package com.github.houbie.lesscss

abstract class OutputCapturingSpec extends AbstractLineFeedFixingSpecification {
    PrintStream originalSystemOut
    ByteArrayOutputStream sysOutCapture = new LineFeedFixingByteArrayOutputStream()
    PrintStream originalSystemErr
    ByteArrayOutputStream sysErrCapture = new LineFeedFixingByteArrayOutputStream()

    def setup() {
        sysOutCapture = new LineFeedFixingByteArrayOutputStream()
        sysErrCapture = new LineFeedFixingByteArrayOutputStream()
        originalSystemOut = System.out
        System.out = new PrintStream(sysOutCapture)
        originalSystemErr = System.err
        System.err = new PrintStream(sysErrCapture)
    }

    def cleanup() {
        System.out = originalSystemOut
        System.err = originalSystemErr
    }

    class LineFeedFixingByteArrayOutputStream extends ByteArrayOutputStream {
        synchronized String toString() {
            return super.toString().replace('\r\n', '\n')
        }
    }
}
