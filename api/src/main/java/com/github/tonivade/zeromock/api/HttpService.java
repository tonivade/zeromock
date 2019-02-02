/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.Matchers.all;
import static com.github.tonivade.zeromock.api.Matchers.startsWith;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Function2;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.PartialFunction1;
import com.github.tonivade.purefun.type.Option;

public class HttpService {
  
  private final String name;
  private final PartialFunction1<HttpRequest, HttpResponse> mappings;
  
  public HttpService(String name) {
    this(name, PartialFunction1.of(Matcher1.never(), Function1.fail()));
  }
  
  private HttpService(String name, PartialFunction1<HttpRequest, HttpResponse> mappings) {
    this.name = requireNonNull(name);
    this.mappings = requireNonNull(mappings);
  }
  
  public String name() {
    return name;
  }

  public HttpService mount(String path, HttpService other) {
    return addMapping(
        startsWith(path).and(req -> other.mappings.isDefinedAt(req.dropOneLevel())), 
        req -> other.mappings.apply(req.dropOneLevel()));
  }
  
  public HttpService exec(RequestHandler handler) {
    return addMapping(all(), handler);
  }
  
  public HttpService add(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    return addMapping(matcher, handler);
  }
  
  public MappingBuilder<HttpService> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }
  
  public Option<HttpResponse> execute(HttpRequest request) {
    return mappings.lift().apply(request);
  }
  
  public HttpService combine(HttpService other) {
    return new HttpService(this.name + "+" + other.name, this.mappings.orElse(other.mappings));
  }
  
  @Override
  public String toString() {
    return "HttpService(" + name + ")";
  }
  
  private HttpService addMapping(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    return new HttpService(name, mappings.orElse(PartialFunction1.of(matcher, handler)));
  }

  public static final class MappingBuilder<T> {
    private final Function2<Matcher1<HttpRequest>, RequestHandler, T> finisher;
    private Matcher1<HttpRequest> matcher;
    
    public MappingBuilder(Function2<Matcher1<HttpRequest>, RequestHandler, T> finisher) {
      this.finisher = requireNonNull(finisher);
    }

    public MappingBuilder<T> when(Matcher1<HttpRequest> matcher) {
      this.matcher = matcher;
      return this;
    }

    public T then(RequestHandler handler) {
      return finisher.apply(matcher, handler);
    }
  }
}
