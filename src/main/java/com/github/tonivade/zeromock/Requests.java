/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.util.Collections.emptyMap;

public class Requests {

  private Requests() {}

  public static Request get(String path) {
    return new Request("GET", new Path(path), null, emptyMap(), emptyMap());
  }

  public static Request post(String path) {
    return new Request("POST", new Path(path), null, emptyMap(), emptyMap());
  }

  public static Request put(String path) {
    return new Request("PUT", new Path(path), null, emptyMap(), emptyMap());
  }

  public static Request delete(String path) {
    return new Request("DELETE", new Path(path), null, emptyMap(), emptyMap());
  }

  public static Request patch(String path) {
    return new Request("PATCH", new Path(path), null, emptyMap(), emptyMap());
  }
}
