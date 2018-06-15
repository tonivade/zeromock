/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.core.Equal.equal;
import static java.util.Objects.nonNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.tonivade.zeromock.core.Consumer2;
import com.github.tonivade.zeromock.core.ImmutableMap;
import com.github.tonivade.zeromock.core.ImmutableSet;
import com.github.tonivade.zeromock.core.Tuple2;

public final class HttpHeaders {
  
  private final ImmutableMap<String, ImmutableSet<String>> headers;
  
  public HttpHeaders(ImmutableMap<String, ImmutableSet<String>> headers) {
    this.headers = Objects.requireNonNull(headers);
  }

  public HttpHeaders withHeader(String key, String value) {
    return new HttpHeaders(headers.merge(key, ImmutableSet.of(value), (a, b) -> a.union(b)));
  }

  public boolean isEmpty() {
    return headers.isEmpty();
  }

  public boolean contains(String key) {
    return headers.containsKey(key);
  }
  
  public ImmutableSet<String> get(String key) {
    return headers.getOrDefault(key, ImmutableSet::empty);
  }
  
  public void forEach(Consumer2<String, String> consumer) {
    headers.forEach((key, values) -> values.forEach(value -> consumer.accept(key, value)));
  }

  public static HttpHeaders empty() {
    return new HttpHeaders(ImmutableMap.empty());
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(headers);
  }
  
  @Override
  public boolean equals(Object obj) {
    return equal(this)
        .append((a, b) -> Objects.equals(a.headers, b.headers))
        .applyTo(obj);
  }

  @Override
  public String toString() {
    return "HttpHeaders(" + headers + ")";
  }
  
  public static HttpHeaders from(Map<String, List<String>> headers) {
    return new HttpHeaders(convert(headers));
  }
  
  private static ImmutableMap<String, ImmutableSet<String>> 
          convert(Map<String, List<String>> headerFields) {
    return ImmutableMap.from(toTuples(headerFields)).mapValues(ImmutableSet::from);
  }

  private static ImmutableSet<Tuple2<String, List<String>>>
          toTuples(Map<String, List<String>> headerFields) {
    return ImmutableSet.from(headerFields.entrySet())
        .filter(entry -> nonNull(entry.getKey())).map(Tuple2::from);
  }
}
