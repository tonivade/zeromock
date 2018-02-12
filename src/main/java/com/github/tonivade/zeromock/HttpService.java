/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Handlers.delegate;
import static com.github.tonivade.zeromock.Predicates.startsWith;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class HttpService {
  private final String name;
  private final Map<Predicate<HttpRequest>, Function<HttpRequest, HttpResponse>> mappings;
  
  public HttpService(String name) {
    this(name, new HashMap<>());
  }
  
  private HttpService(String name, Map<Predicate<HttpRequest>, Function<HttpRequest, HttpResponse>> mappings) {
    this.name = name;
    this.mappings = mappings;
  }
  
  public String name() {
    return name;
  }

  public HttpService mount(String path, HttpService service) {
    mappings.put(startsWith(path), delegate(service));
    return this;
  }
  
  public HttpService when(Predicate<HttpRequest> matcher, Function<HttpRequest, HttpResponse> handler) {
    mappings.put(matcher, handler);
    return this;
  }
  
  public Optional<HttpResponse> handle(HttpRequest request) {
    return findHandler(request).map(handler -> handler.apply(request));
  }
  
  public HttpService combine(HttpService other) {
    Map<Predicate<HttpRequest>, Function<HttpRequest, HttpResponse>> merge = new HashMap<>();
    merge.putAll(this.mappings);
    merge.putAll(other.mappings);
    return new HttpService(this.name + "+" + other.name, merge);
  }

  private Optional<Function<HttpRequest, HttpResponse>> findHandler(HttpRequest request) {
    return mappings.entrySet().stream()
        .filter(entry -> entry.getKey().test(request))
        .map(Map.Entry::getValue)
        .findFirst();
  }
  
  @Override
  public String toString() {
    return "HttpService(" + name + ")";
  }
}
