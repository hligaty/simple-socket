package io.github.hligaty.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author hligaty
 */
@FunctionalInterface
public interface StreamProvider {
    /**
     * @param outputStream socket outputstream
     * @throws IOException if an I/O error occurs
     */
    void send(OutputStream outputStream) throws IOException;
}
