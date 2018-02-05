package com.github.tonivade.zeromock;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class HttpHeaders {
  private Map<String, List<String>> headers;
  
  public HttpHeaders(Map<String, List<String>> headers) {
    this.headers = unmodifiableMap(headers);
  }

  public HttpHeaders withHeader(String key, String value) {
    Map<String, List<String>> newHeaders = new HashMap<>(headers);
    newHeaders.merge(key, singletonList(value), (oldValue, newValue) -> {
      List<String> newList = new ArrayList<>(oldValue);
      newList.addAll(newValue);
      return newList;
    });
    return new HttpHeaders(newHeaders);
  }
  
  public List<String> get(String key) {
    return headers.getOrDefault(key, Collections.emptyList());
  }
  
  public void forEach(BiConsumer<String, String> consumer) {
    headers.forEach((key, values) -> values.forEach(value -> consumer.accept(key, value)));
  }

  public static HttpHeaders empty() {
    return new HttpHeaders(emptyMap());
  }

  @Override
  public String toString() {
    return "HttpHeaders(" + headers + ")";
  }
}
