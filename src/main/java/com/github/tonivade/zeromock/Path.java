/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Path {
  private static final String ROOT = "/";
  private static final String PARAM_PREFIX = ":";

  private final List<PathElement> value;
  
  public Path(String path) {
    this(Stream.of(path.split(ROOT)).skip(1).map(Path::toPathElement).collect(toList()));
  }
  
  private Path(List<PathElement> path) {
    this.value = unmodifiableList(path);
  }
  
  public Path dropOneLevel() {
    return new Path(value.stream().skip(1).collect(toList()));
  }
  
  public PathElement getAt(int pos) {
    return value.get(pos);
  }
  
  public boolean match(Path path) {
    return Pattern.matches(path.toPattern(), toPattern());
  }

  public String toPattern() {
    return ROOT + value.stream().map(PathElement::toPattern).collect(joining(ROOT));
  }
  
  public String toPath() {
    return ROOT + value.stream().map(PathElement::toString).collect(joining(ROOT));
  }
  
  @Override
  public String toString() {
    return "Path(" + value.toString() + ")";
  }
  
  private static PathElement toPathElement(String value) {
    if (value.startsWith(PARAM_PREFIX)) {
      return new PathParam(value.substring(1));
    }
    return new PathValue(value);
  }
  
  private static abstract class PathElement {
    final String value;
    
    public PathElement(String value) {
      this.value = value;
    }
    
    @Override
    public String toString() {
      return value;
    }

    abstract String toPattern();
  }
  
  private static final class PathValue extends PathElement {
    public PathValue(String value) {
      super(value);
    }
    
    @Override
    public String toPattern() {
      return value;
    }
  }
  
  private static final class PathParam extends PathElement {
    public PathParam(String value) {
      super(value);
    }
    
    @Override
    public String toPattern() {
      return "\\w+";
    }
  }
}
