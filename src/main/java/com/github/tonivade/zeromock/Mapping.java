/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import java.util.function.Predicate;

public final class Mapping {
  private final Predicate<HttpRequest> predicate;
  private final Function<HttpRequest, HttpResponse> handler;

  private Mapping(Predicate<HttpRequest> predicate, Function<HttpRequest, HttpResponse> handler) {
    this.predicate = requireNonNull(predicate);
    this.handler = requireNonNull(handler);
  }

  public static MappingBuilder when(Predicate<HttpRequest> matcher) {
    return new MappingBuilder().when(matcher);
  }
  
  public boolean test(HttpRequest request) {
    return predicate.test(request);
  }
  
  public HttpResponse execute(HttpRequest request) {
    return handler.apply(request);
  }

  public static final class MappingBuilder {
    private Predicate<HttpRequest> matcher;

    public MappingBuilder when(Predicate<HttpRequest> matcher) {
      this.matcher = matcher;
      return this;
    }

    public Mapping then(Function<HttpRequest, HttpResponse> handler) {
      return new Mapping(matcher, handler);
    }
  }
}
