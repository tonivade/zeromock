/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.Function1.cons;
import static com.github.tonivade.purefun.effect.URIOOf.toURIO;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.effect.URIO;
import com.github.tonivade.purefun.effect.URIO_;
import com.github.tonivade.purefun.instances.URIOInstances;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.type.Option;

public final class HttpURIOService<R> {

  private final HttpServiceK<Kind<URIO_, R>> serviceK;

  public HttpURIOService(String name) {
    this(new HttpServiceK<>(name, URIOInstances.monad()));
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

  public ThenStep<R, HttpURIOService<R>> preFilter(Matcher1<HttpRequest> matcher) {
    return handler -> addPreFilter(matcher, handler);
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

  public ThenStep<R, HttpURIOService<R>> when(Matcher1<HttpRequest> matcher) {
    return handler -> addMapping(matcher, handler);
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

  protected HttpURIOService<R> addMapping(Matcher1<HttpRequest> matcher, URIORequestHandler<R> handler) {
    return new HttpURIOService<>(serviceK.addMapping(matcher, handler));
  }

  protected HttpURIOService<R> addPreFilter(Matcher1<HttpRequest> matcher, URIORequestHandler<R> handler) {
    return preFilter(filter(URIOInstances.monad(), matcher, handler)::apply);
  }

  @Override
  public String toString() {
    return "HttpURIOService(" + serviceK.name() + ")";
  }
  
  @FunctionalInterface
  public interface ThenStep<R, T> {
    T then(URIORequestHandler<R> handler);
  }
}
