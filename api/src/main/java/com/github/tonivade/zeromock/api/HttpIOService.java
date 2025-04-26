/*
 * Copyright (c) 2018-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;
import com.github.tonivade.purefun.core.Matcher1;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IOOf;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.typeclasses.Instances;

public final class HttpIOService implements HttpRouteBuilderK<IO<?>, HttpIOService> {

  private final HttpServiceK<IO<?>> serviceK;

  public HttpIOService(String name) {
    this(new HttpServiceK<>(name, Instances.<IO<?>>monad()));
  }

  private HttpIOService(HttpServiceK<IO<?>> serviceK) {
    this.serviceK = requireNonNull(serviceK);
  }

  public String name() {
    return serviceK.name();
  }

  public HttpIOService mount(String path, HttpIOService other) {
    return new HttpIOService(serviceK.mount(path, other.serviceK));
  }

  public HttpIOService exec(RequestHandlerK<IO<?>> handler) {
    return new HttpIOService(serviceK.exec(handler));
  }

  public ThenStepK<IO<?>, HttpIOService> preFilter(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serviceK.monad(), handler -> addPreFilter(matcher, handler));
  }

  public HttpIOService preFilter(PreFilter filter) {
    return preFilter(filter.lift(serviceK.monad()));
  }

  public HttpIOService preFilter(PreFilterK<IO<?>> filter) {
    return new HttpIOService(serviceK.preFilter(filter));
  }

  public HttpIOService postFilter(PostFilter filter) {
    return postFilter(filter.lift(serviceK.monad()));
  }

  public HttpIOService postFilter(PostFilterK<IO<?>> filter) {
    return new HttpIOService(serviceK.postFilter(filter));
  }

  @Override
  public ThenStepK<IO<?>, HttpIOService> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serviceK.monad(), handler -> addMapping(matcher, handler));
  }

  public IO<Option<HttpResponse>> execute(HttpRequest request) {
    return serviceK.execute(request).fix(IOOf::toIO);
  }

  public HttpIOService combine(HttpIOService other) {
    return new HttpIOService(serviceK.combine(other.serviceK));
  }

  public HttpServiceK<IO<?>> build() {
    return serviceK;
  }

  public HttpIOService addMapping(Matcher1<HttpRequest> matcher, RequestHandlerK<IO<?>> handler) {
    return new HttpIOService(serviceK.addMapping(matcher, handler));
  }

  private HttpIOService addPreFilter(Matcher1<HttpRequest> matcher, RequestHandlerK<IO<?>> handler) {
    return preFilter(filter(serviceK.monad(), matcher, handler));
  }

  @Override
  public String toString() {
    return "HttpIOService(" + serviceK.name() + ")";
  }
}
