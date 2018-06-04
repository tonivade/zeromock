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
import java.util.function.BiConsumer;

import com.github.tonivade.zeromock.core.InmutableMap;
import com.github.tonivade.zeromock.core.InmutableSet;
import com.github.tonivade.zeromock.core.Tupple2;

public final class HttpHeaders {
  
  private final InmutableMap<String, InmutableSet<String>> headers;
  
  public HttpHeaders(InmutableMap<String, InmutableSet<String>> headers) {
    this.headers = Objects.requireNonNull(headers);
  }

  public HttpHeaders withHeader(String key, String value) {
    return new HttpHeaders(headers.merge(key, InmutableSet.of(value), (a, b) -> a.union(b)));
  }

  public boolean isEmpty() {
    return headers.isEmpty();
  }

  public boolean contains(String key) {
    return headers.containsKey(key);
  }
  
  public InmutableSet<String> get(String key) {
    return headers.getOrDefault(key, InmutableSet::empty);
  }
  
  public void forEach(BiConsumer<String, String> consumer) {
    headers.forEach((key, values) -> values.forEach(value -> consumer.accept(key, value)));
  }

  public static HttpHeaders empty() {
    return new HttpHeaders(InmutableMap.empty());
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
  
  private static InmutableMap<String, InmutableSet<String>> 
          convert(Map<String, List<String>> headerFields) {
    return InmutableMap.from(toTuppleSet(headerFields)).mapValues(InmutableSet::from);
  }

  private static InmutableSet<Tupple2<String, List<String>>>
          toTuppleSet(Map<String, List<String>> headerFields) {
    return InmutableSet.from(headerFields.entrySet())
        .filter(entry -> nonNull(entry.getKey())).map(Tupple2::from);
  }
}
