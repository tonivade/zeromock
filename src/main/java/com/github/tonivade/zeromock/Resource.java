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
  private final Map<Predicate<Request>, Function<Request, Response>> mappings = new HashMap<>();
  
  public Resource(String name) {
    this.name = name;
  }
  
  public Resource when(Predicate<Request> matcher, Function<Request, Response> handler) {
    mappings.put(matcher, handler);
    return this;
  }
  
  public Response handle(Request request) {
    return findHandler(request).apply(request);
  }

  private Function<Request, Response> findHandler(Request request) {
    return mappings.entrySet().stream()
        .filter(entry -> entry.getKey().test(request))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse(notFound("not found"));
  }
}
