package io.github.hligaty.util;

import java.io.IOException;
import java.io.OutputStream;

@FunctionalInterface
public interface Sender {
    void send(OutputStream outputStream) throws IOException;
}
