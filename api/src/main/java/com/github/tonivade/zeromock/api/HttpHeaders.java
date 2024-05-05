/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.tonivade.purefun.core.Consumer2;
import com.github.tonivade.purefun.core.Equal;
import com.github.tonivade.purefun.core.Tuple;
import com.github.tonivade.purefun.core.Tuple2;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.data.ImmutableSet;

public final class HttpHeaders implements Iterable<Tuple2<String, String>> {

  private static final Equal<HttpHeaders> EQUAL = Equal.<HttpHeaders>of()
      .comparing(h -> h.headers);

  private final ImmutableMap<String, ImmutableSet<String>> headers;

  public HttpHeaders(ImmutableMap<String, ImmutableSet<String>> headers) {
    this.headers = requireNonNull(headers).mapKeys(String::toLowerCase);
  }

  @Override
  public Iterator<Tuple2<String, String>> iterator() {
    return headers.entries()
        .flatMap(t -> t.applyTo((key, values) -> values.map(v -> Tuple2.of(key, v)))).iterator();
  }

  public HttpHeaders withHeader(String key, String value) {
    return new HttpHeaders(headers.merge(key.toLowerCase(), ImmutableSet.of(value), ImmutableSet::union));
  }

  public boolean isEmpty() {
    return headers.isEmpty();
  }

  public boolean contains(String key) {
    return headers.containsKey(key.toLowerCase());
  }

  public ImmutableSet<String> get(String key) {
    return headers.getOrDefault(key.toLowerCase(), ImmutableSet::empty);
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
    return EQUAL.applyTo(this, obj);
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
        .filter(entry -> nonNull(entry.getKey())).map(Tuple::from);
  }
}
