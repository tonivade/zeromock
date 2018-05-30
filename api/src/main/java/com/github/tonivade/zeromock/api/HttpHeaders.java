/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.core.Equal.equal;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import com.github.tonivade.zeromock.core.InmutableSet;

public final class HttpHeaders {
  
  private final Map<String, InmutableSet<String>> headers;
  
  public HttpHeaders(Map<String, InmutableSet<String>> headers) {
    this.headers = unmodifiableMap(headers);
  }

  public HttpHeaders withHeader(String key, String value) {
    Map<String, InmutableSet<String>> newHeaders = new HashMap<>(headers);
    newHeaders.merge(key, InmutableSet.of(value), (oldValue, newValue) -> {
      return oldValue.union(newValue);
    });
    return new HttpHeaders(newHeaders);
  }

  public boolean isEmpty() {
    return headers.isEmpty();
  }

  public boolean contains(String key) {
    return headers.containsKey(key);
  }
  
  public InmutableSet<String> get(String key) {
    return headers.getOrDefault(key, InmutableSet.empty());
  }
  
  public void forEach(BiConsumer<String, String> consumer) {
    headers.forEach((key, values) -> values.forEach(value -> consumer.accept(key, value)));
  }

  public static HttpHeaders empty() {
    return new HttpHeaders(emptyMap());
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
  
  private static Map<String, InmutableSet<String>> convert(Map<String, List<String>> headerFields) {
    return headerFields.entrySet().stream()
        .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), InmutableSet.from(entry.getValue())))
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
