/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

public class Path {
  private List<String> value;
  
  public Path(String path) {
    this(Stream.of(path.split("/")).skip(1).collect(toList()));
  }
  
  private Path(List<String> path) {
    this.value = path;
  }
  
  public Path dropOneLevel() {
    return new Path(value.stream().skip(1).collect(toList()));
  }
  
  public String get(int pos) {
    return value.get(pos);
  }
  
  public boolean match(String url) {
    return url.equals(toString());
  }

  @Override
  public String toString() {
    return "/" + value.stream().collect(joining("/"));
  }
}
