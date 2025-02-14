package org.codehaus.mojo.exec;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An output stream that captures one line of output at a time, and then
 * redirects that line to some {@link Consumer} to act upon as it pleases. This
 * class is not thread safe and expects to have only one active writer consuming
 * it at any given time.
 * 
 * @since 3.0.0
 */
class LineRedirectOutputStream extends OutputStream {

    private final Consumer<String> linePrinter;
    private final Charset charset;
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public LineRedirectOutputStream(Consumer<String> linePrinter) {
        this(linePrinter, Charset.defaultCharset());
    }

    public LineRedirectOutputStream(Consumer<String> linePrinter, Charset charset) {
        this.linePrinter = Objects.requireNonNull(linePrinter);
        this.charset = Objects.requireNonNull(charset);
    }

    @Override
    public void write(final int b) {
        if ((char) b == '\n') {
            printAndReset();
            return;
        }
        buffer.write(b);
    }

    @Override
    public void flush() {
        if (buffer.size() > 0) {
            printAndReset();
        }
    }

    @Override
    public void close() {
        flush();
    }

    private void printAndReset() {
        linePrinter.accept(new String(buffer.toByteArray(), charset));
        buffer = new ByteArrayOutputStream();
    }
}
