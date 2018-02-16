/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.nio.ByteBuffer.wrap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public final class Bytes {

  private static final int BUFFER_SIZE = 1024;
  private static final Charset UTF8 = Charset.forName("UTF-8");

  private Bytes() {}

  public static ByteBuffer emptyByteBuffer() {
    return wrap(new byte[]{});
  }

  public static ByteBuffer asByteBuffer(InputStream input) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[BUFFER_SIZE];
    while (true) {
      int read = input.read(buffer);
      if (read > 0) {
        out.write(buffer, 0, read);
      } else break;
    }
    return wrap(out.toByteArray());
  }
  
  public static ByteBuffer asByteBuffer(String string) {
    return wrap(string.getBytes(Bytes.UTF8));
  }
  
  public static String asString(ByteBuffer buffer) {
    return new String(buffer.array(), Bytes.UTF8);
  }
}
