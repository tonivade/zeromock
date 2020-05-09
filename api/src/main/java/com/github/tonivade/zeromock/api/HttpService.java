/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.PreFilter.filter;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Function2;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.instances.IdInstances;
import com.github.tonivade.purefun.type.Id;
import com.github.tonivade.purefun.type.Option;

public final class HttpService {

  private final HttpServiceK<Id.µ> serviceK;

  public HttpService(String name) {
    this(new HttpServiceK<>(name, IdInstances.monad()));
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
    return new HttpService(serviceK.exec(handler.liftId()::apply));
  }

  public MappingBuilder<HttpService> preFilter(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::addPreFilter).when(requireNonNull(matcher));
  }

  public HttpService preFilter(PreFilter filter) {
    return new HttpService(serviceK.preFilter(filter.liftId()::apply));
  }

  public HttpService postFilter(PostFilter filter) {
    return new HttpService(serviceK.postFilter(filter));
  }

  public MappingBuilder<HttpService> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::addMapping).when(matcher);
  }

  public Option<HttpResponse> execute(HttpRequest request) {
    return serviceK.execute(request).fix1(Id::narrowK).get();
  }

  public HttpService combine(HttpService other) {
    return new HttpService(this.serviceK.combine(other.serviceK));
  }

  protected HttpService addMapping(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    return new HttpService(serviceK.addMapping(matcher, handler.liftId()::apply));
  }

  protected HttpService addPreFilter(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    return preFilter(filter(matcher, handler));
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
