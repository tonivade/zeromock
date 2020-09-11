/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.UIOOf;
import com.github.tonivade.purefun.effect.UIO_;
import com.github.tonivade.purefun.instances.UIOInstances;
import com.github.tonivade.purefun.type.Option;

public final class HttpUIOService {

  private final HttpServiceK<UIO_> serviceK;

  public HttpUIOService(String name) {
    this(new HttpServiceK<>(name, UIOInstances.monad()));
  }

  private HttpUIOService(HttpServiceK<UIO_> serviceK) {
    this.serviceK = requireNonNull(serviceK);
  }

  public String name() {
    return serviceK.name();
  }

  public HttpUIOService mount(String path, HttpUIOService other) {
    return new HttpUIOService(serviceK.mount(path, other.serviceK));
  }

  public HttpUIOService exec(UIORequestHandler handler) {
    return new HttpUIOService(serviceK.exec(handler));
  }

  public ThenStep<HttpUIOService> preFilter(Matcher1<HttpRequest> matcher) {
    return handler -> addPreFilter(matcher, handler);
  }

  public HttpUIOService preFilter(PreFilter filter) {
    return preFilter(filter.andThen(UIO::pure)::apply);
  }

  public HttpUIOService preFilter(UIOPreFilter filter) {
    return new HttpUIOService(serviceK.preFilter(filter));
  }

  public HttpUIOService postFilter(PostFilter filter) {
    return postFilter(filter.andThen(UIO::pure)::apply);
  }

  public HttpUIOService postFilter(UIOPostFilter filter) {
    return new HttpUIOService(serviceK.postFilter(filter));
  }

  public ThenStep<HttpUIOService> when(Matcher1<HttpRequest> matcher) {
    return handler -> addMapping(matcher, handler);
  }

  public UIO<Option<HttpResponse>> execute(HttpRequest request) {
    return serviceK.execute(request).fix(UIOOf::narrowK);
  }

  public HttpUIOService combine(HttpUIOService other) {
    return new HttpUIOService(serviceK.combine(other.serviceK));
  }

  public HttpServiceK<UIO_> build() {
    return serviceK;
  }

  protected HttpUIOService addMapping(Matcher1<HttpRequest> matcher, UIORequestHandler handler) {
    return new HttpUIOService(serviceK.addMapping(matcher, handler));
  }

  protected HttpUIOService addPreFilter(Matcher1<HttpRequest> matcher, UIORequestHandler handler) {
    return preFilter(filter(UIOInstances.monad(), matcher, handler)::apply);
  }

  @Override
  public String toString() {
    return "HttpUIOService(" + serviceK.name() + ")";
  }
  
  @FunctionalInterface
  public interface ThenStep<T> {
    T then(UIORequestHandler handler);
  }
}
