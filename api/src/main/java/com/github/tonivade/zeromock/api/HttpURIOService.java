/*
 * Copyright (c) 2018-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.core.Matcher1;
import com.github.tonivade.purefun.effect.URIO;
import com.github.tonivade.purefun.effect.URIOOf;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.typeclasses.Instances;

public final class HttpURIOService<R> implements HttpRouteBuilderK<URIO<R, ?>, HttpURIOService<R>> {

  private final HttpServiceK<URIO<R, ?>> serviceK;

  public HttpURIOService(String name) {
    this(new HttpServiceK<>(name, Instances.<URIO<R, ?>>monad()));
  }

  private HttpURIOService(HttpServiceK<URIO<R, ?>> serviceK) {
    this.serviceK = requireNonNull(serviceK);
  }

  public String name() {
    return serviceK.name();
  }

  public HttpURIOService<R> mount(String path, HttpURIOService<R> other) {
    return new HttpURIOService<>(this.serviceK.mount(path, other.serviceK));
  }

  public HttpURIOService<R> exec(RequestHandlerK<URIO<R, ?>> handler) {
    return new HttpURIOService<>(serviceK.exec(handler));
  }

  public ThenStepK<URIO<R, ?>, HttpURIOService<R>> preFilter(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serviceK.monad(), handler -> addPreFilter(matcher, handler));
  }

  public HttpURIOService<R> preFilter(PreFilter filter) {
    return preFilter(filter.lift(serviceK.monad()));
  }

  public HttpURIOService<R> preFilter(PreFilterK<URIO<R, ?>> filter) {
    return new HttpURIOService<>(serviceK.preFilter(filter));
  }

  public HttpURIOService<R> postFilter(PostFilter filter) {
    return postFilter(filter.lift(serviceK.monad()));
  }

  public HttpURIOService<R> postFilter(PostFilterK<URIO<R, ?>> filter) {
    return new HttpURIOService<>(serviceK.postFilter(filter));
  }

  @Override
  public ThenStepK<URIO<R, ?>, HttpURIOService<R>> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serviceK.monad(), handler -> addMapping(matcher, handler));
  }

  public URIO<R, Option<HttpResponse>> execute(HttpRequest request) {
    return serviceK.execute(request).fix(URIOOf::toURIO);
  }

  public HttpURIOService<R> combine(HttpURIOService<R> other) {
    return new HttpURIOService<>(this.serviceK.combine(other.serviceK));
  }

  public HttpServiceK<URIO<R, ?>> build() {
    return serviceK;
  }

  private HttpURIOService<R> addMapping(Matcher1<HttpRequest> matcher, RequestHandlerK<URIO<R, ?>> handler) {
    return new HttpURIOService<>(serviceK.addMapping(matcher, handler));
  }

  private HttpURIOService<R> addPreFilter(Matcher1<HttpRequest> matcher, RequestHandlerK<URIO<R, ?>> handler) {
    return preFilter(filter(serviceK.monad(), matcher, handler));
  }

  @Override
  public String toString() {
    return "HttpURIOService(" + serviceK.name() + ")";
  }
}
