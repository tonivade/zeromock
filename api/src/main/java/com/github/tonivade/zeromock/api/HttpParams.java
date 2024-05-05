/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.github.tonivade.purefun.core.Equal;
import com.github.tonivade.purefun.core.Tuple2;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.type.Option;

public final class HttpParams {

  private static final Equal<HttpParams> EQUAL = Equal.<HttpParams>of()
      .comparing(h -> h.params);

  private static final String BEGIN = "?";
  private static final String EMPTY = "";
  private static final String EQUALS = "=";
  private static final String SEPARATOR = "&";

  private final ImmutableMap<String, String> params;

  public HttpParams(String queryParams) {
    this(queryToMap(queryParams));
  }

  public HttpParams(ImmutableMap<String, String> params) {
    this.params = requireNonNull(params);
  }

  public Option<String> get(String name) {
    return params.get(name);
  }

  public boolean isEmpty() {
    return params.isEmpty();
  }

  public boolean contains(String name) {
    return params.containsKey(name);
  }

  public HttpParams withParam(String key, String value) {
    return new HttpParams(params.put(key, value));
  }

  public String toQueryString() {
    return params.isEmpty() ? EMPTY : paramsToString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(params);
  }

  @Override
  public boolean equals(Object obj) {
    return EQUAL.applyTo(this, obj);
  }

  @Override
  public String toString() {
    return "HttpParams(" + params + ")";
  }

  public static HttpParams empty() {
    return new HttpParams(ImmutableMap.empty());
  }

  private static ImmutableMap<String, String> queryToMap(String query) {
    Map<String, String> result = new HashMap<>();
    if (query != null) {
      for (String param : query.split(SEPARATOR)) {
        String[] pair = param.split(EQUALS);
        if (pair.length > 1) {
          result.put(pair[0], urlDecode(pair[1]));
        } else {
          result.put(pair[0], EMPTY);
        }
      }
    }
    return ImmutableMap.from(result);
  }

  private String paramsToString() {
    return BEGIN + params.entries().stream()
        .map(entryToString()).collect(joining(SEPARATOR));
  }

  private Function<Tuple2<String, String>, String> entryToString() {
    return entry -> entry.get1() + EQUALS + urlEncode(entry.get2());
  }

  private static String urlEncode(String value) {
    return URLEncoder.encode(value, UTF_8);
  }

  private static String urlDecode(String value) {
    return URLDecoder.decode(value, UTF_8);
  }
}
