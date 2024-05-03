/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.effect.UIOOf.toUIO;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.core.Matcher1;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.typeclasses.Instances;

public final class HttpUIOService implements HttpRouteBuilderK<UIO<?>, HttpUIOService> {

  private final HttpServiceK<UIO<?>> serviceK;

  public HttpUIOService(String name) {
    this(name, Future.DEFAULT_EXECUTOR);
  }

  public HttpUIOService(String name, Executor executor) {
    this(new HttpServiceK<>(name, Instances.monad()));
  }

  private HttpUIOService(HttpServiceK<UIO<?>> serviceK) {
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

  public ThenStepK<UIO<?>, HttpUIOService> preFilter(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serviceK.monad(), handler -> addPreFilter(matcher, handler::apply));
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

  @Override
  public ThenStepK<UIO<?>, HttpUIOService> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serviceK.monad(), handler -> addMapping(matcher, handler::apply));
  }

  public UIO<Option<HttpResponse>> execute(HttpRequest request) {
    return serviceK.execute(request).fix(toUIO());
  }

  public HttpUIOService combine(HttpUIOService other) {
    return new HttpUIOService(serviceK.combine(other.serviceK));
  }

  public HttpServiceK<UIO<?>> build() {
    return serviceK;
  }

  private HttpUIOService addMapping(Matcher1<HttpRequest> matcher, UIORequestHandler handler) {
    return new HttpUIOService(serviceK.addMapping(matcher, handler));
  }

  private HttpUIOService addPreFilter(Matcher1<HttpRequest> matcher, UIORequestHandler handler) {
    return preFilter(filter(serviceK.monad(), matcher, handler)::apply);
  }

  @Override
  public String toString() {
    return "HttpUIOService(" + serviceK.name() + ")";
  }
}
