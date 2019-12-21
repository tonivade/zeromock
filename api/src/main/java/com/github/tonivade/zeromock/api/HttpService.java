/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Function2;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.type.Id;
import com.github.tonivade.purefun.type.Option;

public final class HttpService {

  private HttpServiceK<Id.µ> serviceK;

  public HttpService(String name) {
    this.serviceK = new HttpServiceK<>(name);
  }

  private HttpService(HttpServiceK<Id.µ> serviceK) {
    this.serviceK = requireNonNull(serviceK);
  }

  public String name() {
    return serviceK.name();
  }

  public HttpServiceK<Id.µ> build() {
    return serviceK;
  }

  public HttpService mount(String path, HttpService other) {
    return new HttpService(serviceK.mount(path, other.serviceK));
  }

  public HttpService exec(RequestHandler handler) {
    return new HttpService(serviceK.exec(handler.sync()));
  }

  public HttpService add(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    return new HttpService(serviceK.add(matcher, handler.sync()));
  }

  public MappingBuilder<HttpService> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }

  public Option<HttpResponse> execute(HttpRequest request) {
    return serviceK.execute(request).map(Id::narrowK).map(Id::get);
  }

  public HttpService combine(HttpService other) {
    return new HttpService(this.serviceK.combine(other.serviceK));
  }

  @Override
  public String toString() {
    return "HttpService(" + serviceK.name() + ")";
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
