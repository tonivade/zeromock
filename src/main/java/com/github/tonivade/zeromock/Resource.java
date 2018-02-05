/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Responses.notFound;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class Resource {
  final String name;
  private final Map<Predicate<HttpRequest>, Function<HttpRequest, HttpResponse>> mappings = new HashMap<>();
  
  public Resource(String name) {
    this.name = name;
  }
  
  public Resource when(Predicate<HttpRequest> matcher, Function<HttpRequest, HttpResponse> handler) {
    mappings.put(matcher, handler);
    return this;
  }
  
  public HttpResponse handle(HttpRequest request) {
    return findHandler(request).apply(request);
  }

  private Function<HttpRequest, HttpResponse> findHandler(HttpRequest request) {
    return mappings.entrySet().stream()
        .filter(entry -> entry.getKey().test(request))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse(notFound("not found"));
  }
  
  @Override
  public String toString() {
    return "Resource(" + name + ")";
  }
}
