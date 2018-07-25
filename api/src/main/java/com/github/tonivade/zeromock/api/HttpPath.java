/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.type.Equal.comparing;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

import java.util.Objects;
import java.util.regex.Pattern;

import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.type.Equal;
import com.github.tonivade.purefun.type.Option;

public final class HttpPath {
  
  private static final String ROOT = "/";
  private static final String PARAM_PREFIX = ":";

  private final ImmutableList<PathElement> value;
  
  private HttpPath(ImmutableList<PathElement> path) {
    this.value = Objects.requireNonNull(path);
  }
  
  public HttpPath dropOneLevel() {
    return new HttpPath(value.tail());
  }
  
  public int size() {
    return value.size();
  }
  
  public Option<PathElement> getAt(int position) {
    return value.drop(position).head();
  }
  
  public boolean match(HttpPath other) {
    return Pattern.matches(other.toPattern(), this.toPattern());
  }

  public boolean startsWith(HttpPath other) {
    return Pattern.matches(other.toPattern() + ".*", this.toPattern());
  }
  
  public String toPath() {
    return ROOT + value.stream().map(PathElement::toString).collect(joining(ROOT));
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
  
  @Override
  public boolean equals(Object obj) {
    return Equal.of(this)
        .append((a, b) -> Objects.equals(a.value, b.value))
        .applyTo(obj);
  }
  
  @Override
  public String toString() {
    return "Path(" + value.toString() + ")";
  }

  private String toPattern() {
    return ROOT + value.stream().map(PathElement::toPattern).collect(joining(ROOT));
  }

  public static HttpPath of(String... path) {
    if (isNull(path)) {
      throw new IllegalArgumentException("invalid path definition: " + path);
    }
    return new HttpPath(ImmutableList.of(path).map(HttpPath::toPathElement));
  }
  
  public static HttpPath from(String path) {
    if (isNull(path) || path.isEmpty() || !path.startsWith(ROOT)) {
      throw new IllegalArgumentException("invalid path: " + path);
    }
    return new HttpPath(ImmutableList.of(path.split(ROOT)).tail().map(HttpPath::toPathElement));
  }
  
  private static PathElement toPathElement(String value) {
    if (value.startsWith(PARAM_PREFIX)) {
      return new PathParam(value.substring(1));
    }
    return new PathValue(value);
  }
  
  public abstract static class PathElement {
    private final String value;
    
    private PathElement(String value) {
      this.value = Objects.requireNonNull(value);
    }
    
    public String value() {
      return value;
    }
  
    @Override
    public int hashCode() {
      return Objects.hash(value);
    }
    
    @Override
    public boolean equals(Object obj) {
      return Equal.of(this)
          .append(comparing(PathElement::value))
          .applyTo(obj);
    }
    
    @Override
    public String toString() {
      return value;
    }

    protected abstract String toPattern();
  }
  
  private static final class PathValue extends PathElement {
    private PathValue(String value) {
      super(value);
    }
    
    @Override
    protected String toPattern() {
      return super.value();
    }
  }
  
  private static final class PathParam extends PathElement {
    private PathParam(String value) {
      super(value);
    }
    
    @Override
    protected String toPattern() {
      return "\\w+";
    }
  }
}
