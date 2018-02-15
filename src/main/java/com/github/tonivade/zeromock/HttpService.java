/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Handlers.delegate;
import static com.github.tonivade.zeromock.Predicates.startsWith;
import static java.util.Objects.requireNonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class HttpService {
  
  private final String name;
  private final List<Mapping> mappings;
  
  public HttpService(String name) {
    this(name, new LinkedList<>());
  }
  
  private HttpService(String name, List<Mapping> mappings) {
    this.name = requireNonNull(name);
    this.mappings = requireNonNull(mappings);
  }
  
  public String name() {
    return name;
  }

  public HttpService mount(String path, HttpService service) {
    mappings.add(mapping(startsWith(path), delegate(service)));
    return this;
  }
  
  public HttpService when(Predicate<HttpRequest> matcher, Function<HttpRequest, HttpResponse> handler) {
    mappings.add(mapping(matcher, handler));
    return this;
  }
  
  public Optional<HttpResponse> execute(HttpRequest request) {
    return findMapping(request).map(mapping -> mapping.execute(request));
  }
  
  public HttpService combine(HttpService other) {
    List<Mapping> merge = new LinkedList<>();
    merge.addAll(this.mappings);
    merge.addAll(other.mappings);
    return new HttpService(this.name + "+" + other.name, merge);
  }
  
  @Override
  public String toString() {
    return "HttpService(" + name + ")";
  }

  private Optional<Mapping> findMapping(HttpRequest request) {
    return mappings.stream()
        .filter(mapping -> mapping.test(request))
        .findFirst();
  }
  
  private Mapping mapping(Predicate<HttpRequest> predicate, Function<HttpRequest, HttpResponse> handler) {
    return new Mapping(predicate, handler);
  }
  
  private static final class Mapping {
    private final Predicate<HttpRequest> predicate;
    private final Function<HttpRequest, HttpResponse> handler;

    public Mapping(Predicate<HttpRequest> predicate, Function<HttpRequest, HttpResponse> handler) {
      this.predicate = requireNonNull(predicate);
      this.handler = requireNonNull(handler);
    }
    
    public boolean test(HttpRequest request) {
      return predicate.test(request);
    }
    
    public HttpResponse execute(HttpRequest request) {
      return handler.apply(request);
    }
  }
}
