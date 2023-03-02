/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.Function1.cons;
import static com.github.tonivade.purefun.effect.URIOOf.toURIO;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.effect.URIO;
import com.github.tonivade.purefun.effect.URIO_;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.typeclasses.Instance;

public final class HttpURIOService<R> implements HttpRouteBuilderK<Kind<URIO_, R>, HttpURIOService<R>> {

  private final HttpServiceK<Kind<URIO_, R>> serviceK;

  public HttpURIOService(String name) {
    this(name, Future.DEFAULT_EXECUTOR);
  }

  public HttpURIOService(String name, Executor executor) {
    this(new HttpServiceK<>(name, new Instance<Kind<URIO_, R>>() {}.concurrent(executor)));
  }

  private HttpURIOService(HttpServiceK<Kind<URIO_, R>> serviceK) {
    this.serviceK = requireNonNull(serviceK);
  }

  public String name() {
    return serviceK.name();
  }

  public HttpURIOService<R> mount(String path, HttpURIOService<R> other) {
    return new HttpURIOService<>(this.serviceK.mount(path, other.serviceK));
  }

  public HttpURIOService<R> exec(URIO<R, HttpResponse> method) {
    return new HttpURIOService<>(serviceK.exec(cons(method)::apply));
  }

  public ThenStepK<Kind<URIO_, R>, HttpURIOService<R>> preFilter(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serviceK.monad(), handler -> addPreFilter(matcher, handler::apply));
  }

  public HttpURIOService<R> preFilter(PreFilter filter) {
    return preFilter(filter.andThen(URIO::<R, Either<HttpResponse, HttpRequest>>pure)::apply);
  }

  public HttpURIOService<R> preFilter(URIOPreFilter<R> filter) {
    return new HttpURIOService<>(serviceK.preFilter(filter));
  }

  public HttpURIOService<R> postFilter(PostFilter filter) {
    return postFilter(filter.andThen(URIO::<R, HttpResponse>pure)::apply);
  }

  public HttpURIOService<R> postFilter(URIOPostFilter<R> filter) {
    return new HttpURIOService<>(serviceK.postFilter(filter));
  }

  @Override
  public ThenStepK<Kind<URIO_, R>, HttpURIOService<R>> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serviceK.monad(), handler -> addMapping(matcher, handler::apply));
  }

  public URIO<R, Option<HttpResponse>> execute(HttpRequest request) {
    return serviceK.execute(request).fix(toURIO());
  }

  public HttpURIOService<R> combine(HttpURIOService<R> other) {
    return new HttpURIOService<>(this.serviceK.combine(other.serviceK));
  }

  public HttpServiceK<Kind<URIO_, R>> build() {
    return serviceK;
  }

  private HttpURIOService<R> addMapping(Matcher1<HttpRequest> matcher, URIORequestHandler<R> handler) {
    return new HttpURIOService<>(serviceK.addMapping(matcher, handler));
  }

  private HttpURIOService<R> addPreFilter(Matcher1<HttpRequest> matcher, URIORequestHandler<R> handler) {
    return preFilter(filter(serviceK.monad(), matcher, handler)::apply);
  }

  @Override
  public String toString() {
    return "HttpURIOService(" + serviceK.name() + ")";
  }
}
