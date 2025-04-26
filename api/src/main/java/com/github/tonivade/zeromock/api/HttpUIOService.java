/*
 * Copyright (c) 2018-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;
import com.github.tonivade.purefun.core.Matcher1;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.UIOOf;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.typeclasses.Instances;

public final class HttpUIOService implements HttpRouteBuilderK<UIO<?>, HttpUIOService> {

  private final HttpServiceK<UIO<?>> serviceK;

  public HttpUIOService(String name) {
    this(new HttpServiceK<>(name, Instances.<UIO<?>>monad()));
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

  public HttpUIOService exec(RequestHandlerK<UIO<?>> handler) {
    return new HttpUIOService(serviceK.exec(handler));
  }

  public ThenStepK<UIO<?>, HttpUIOService> preFilter(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serviceK.monad(), handler -> addPreFilter(matcher, handler));
  }

  public HttpUIOService preFilter(PreFilter filter) {
    return preFilter(filter.lift(serviceK.monad()));
  }

  public HttpUIOService preFilter(PreFilterK<UIO<?>> filter) {
    return new HttpUIOService(serviceK.preFilter(filter));
  }

  public HttpUIOService postFilter(PostFilter filter) {
    return postFilter(filter.lift(serviceK.monad()));
  }

  public HttpUIOService postFilter(PostFilterK<UIO<?>> filter) {
    return new HttpUIOService(serviceK.postFilter(filter));
  }

  @Override
  public ThenStepK<UIO<?>, HttpUIOService> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serviceK.monad(), handler -> addMapping(matcher, handler));
  }

  public UIO<Option<HttpResponse>> execute(HttpRequest request) {
    return serviceK.execute(request).fix(UIOOf::toUIO);
  }

  public HttpUIOService combine(HttpUIOService other) {
    return new HttpUIOService(serviceK.combine(other.serviceK));
  }

  public HttpServiceK<UIO<?>> build() {
    return serviceK;
  }

  private HttpUIOService addMapping(Matcher1<HttpRequest> matcher, RequestHandlerK<UIO<?>> handler) {
    return new HttpUIOService(serviceK.addMapping(matcher, handler));
  }

  private HttpUIOService addPreFilter(Matcher1<HttpRequest> matcher, RequestHandlerK<UIO<?>> handler) {
    return preFilter(filter(serviceK.monad(), matcher, handler));
  }

  @Override
  public String toString() {
    return "HttpUIOService(" + serviceK.name() + ")";
  }
}
