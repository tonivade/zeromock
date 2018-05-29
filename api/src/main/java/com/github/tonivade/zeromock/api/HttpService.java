/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.Matchers.all;
import static com.github.tonivade.zeromock.api.Matchers.startsWith;
import static com.github.tonivade.zeromock.core.Handler1.adapt;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.zeromock.core.Handler2;
import com.github.tonivade.zeromock.core.InmutableList;
import com.github.tonivade.zeromock.core.Matcher;
import com.github.tonivade.zeromock.core.Option;
import com.github.tonivade.zeromock.core.OptionHandler;

public class HttpService {
  
  private final String name;
  private final InmutableList<Mapping> mappings;
  
  public HttpService(String name) {
    this(name, InmutableList.empty());
  }
  
  private HttpService(String name, InmutableList<Mapping> mappings) {
    this.name = requireNonNull(name);
    this.mappings = requireNonNull(mappings);
  }
  
  public String name() {
    return name;
  }

  public HttpService mount(String path, HttpService service) {
    return new HttpService(name, addMapping(startsWith(path), adapt(HttpRequest::dropOneLevel).andThen(service::execute)::handle));
  }
  
  public HttpService exec(RequestHandler handler) {
    return new HttpService(name, addMapping(all(), handler.liftOption()));
  }
  
  public HttpService add(Matcher<HttpRequest> matcher, RequestHandler handler) {
    return new HttpService(name, addMapping(matcher, handler.liftOption()));
  }
  
  public MappingBuilder<HttpService> when(Matcher<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }
  
  public Option<HttpResponse> execute(HttpRequest request) {
    return findMapping(request).flatMap(mapping -> mapping.handle(request));
  }
  
  public HttpService combine(HttpService other) {
    return new HttpService(this.name + "+" + other.name, this.mappings.concat(other.mappings));
  }
  
  @Override
  public String toString() {
    return "HttpService(" + name + ")";
  }

  public HttpService clear() {
    return new HttpService(this.name);
  }
  
  private InmutableList<Mapping> addMapping(Matcher<HttpRequest> matcher, OptionHandler<HttpRequest, HttpResponse> handler) {
    return mappings.add(new Mapping(matcher, handler));
  }

  private Option<Mapping> findMapping(HttpRequest request) {
    return mappings
        .filter(mapping -> mapping.match(request))
        .head();
  }

  public static final class MappingBuilder<T> {
    private final Handler2<Matcher<HttpRequest>, RequestHandler, T> finisher;
    private Matcher<HttpRequest> matcher;
    
    public MappingBuilder(Handler2<Matcher<HttpRequest>, RequestHandler, T> finisher) {
      this.finisher = requireNonNull(finisher);
    }

    public MappingBuilder<T> when(Matcher<HttpRequest> matcher) {
      this.matcher = matcher;
      return this;
    }

    public T then(RequestHandler handler) {
      return finisher.handle(matcher, handler);
    }
  }
  
  public static final class Mapping {
    private final Matcher<HttpRequest> matcher;
    private final OptionHandler<HttpRequest, HttpResponse> handler;

    private Mapping(Matcher<HttpRequest> matcher, OptionHandler<HttpRequest, HttpResponse> handler) {
      this.matcher = requireNonNull(matcher);
      this.handler = requireNonNull(handler);
    }

    public boolean match(HttpRequest request) {
      return matcher.match(request);
    }

    public Option<HttpResponse> handle(HttpRequest request) {
      return handler.handle(request);
    }
  }
}
