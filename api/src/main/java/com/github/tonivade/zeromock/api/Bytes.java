/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static java.nio.ByteBuffer.wrap;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.github.tonivade.purefun.Equal;

public final class Bytes {

  private static final Equal<Bytes> EQUAL = Equal.<Bytes>of()
      .append((a, b) -> Arrays.equals(a.buffer, b.buffer));
  
  private static final int BUFFER_SIZE = 1024;
  private static final Bytes EMPTY = new Bytes(new byte[]{});

  private final byte[] buffer;

  private Bytes(byte[] buffer) {
    this.buffer = requireNonNull(buffer);
  }

  public byte[] toArray() {
    byte[] copy = new byte[buffer.length];
    System.arraycopy(buffer, 0, copy, 0, buffer.length);
    return copy;
  }

  public ByteBuffer getBuffer() {
    return wrap(buffer);
  }

  public int size() {
    return buffer.length;
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(buffer);
  }

  @Override
  public boolean equals(Object obj) {
    return EQUAL.applyTo(this, obj);
  }

  public static Bytes empty() {
    return EMPTY;
  }

  public static Bytes fromArray(byte[] array) {
    return new Bytes(array);
  }

  public static Bytes asBytes(InputStream input) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[BUFFER_SIZE];
    while (true) {
      int read = input.read(buffer);
      if (read > 0) {
        out.write(buffer, 0, read);
      } else break;
    }
    return new Bytes(out.toByteArray());
  }

  public static Bytes asBytes(String string) {
    return new Bytes(string.getBytes(UTF_8));
  }

  public static String asString(Bytes buffer) {
    return new String(buffer.toArray(), UTF_8);
  }
}
