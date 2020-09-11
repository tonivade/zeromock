/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.Function1.cons;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.effect.ZIO;
import com.github.tonivade.purefun.effect.ZIOOf;
import com.github.tonivade.purefun.effect.ZIO_;
import com.github.tonivade.purefun.instances.ZIOInstances;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.type.Option;

public final class HttpZIOService<R> {

  private final HttpServiceK<Kind<Kind<ZIO_, R>, Nothing>> serviceK;

  public HttpZIOService(String name) {
    this(new HttpServiceK<>(name, ZIOInstances.monad()));
  }

  private HttpZIOService(HttpServiceK<Kind<Kind<ZIO_, R>, Nothing>> serviceK) {
    this.serviceK = requireNonNull(serviceK);
  }

  public String name() {
    return serviceK.name();
  }

  public HttpZIOService<R> mount(String path, HttpZIOService<R> other) {
    return new HttpZIOService<>(this.serviceK.mount(path, other.serviceK));
  }

  public HttpZIOService<R> exec(ZIO<R, Nothing, HttpResponse> method) {
    return new HttpZIOService<>(serviceK.exec(cons(method)::apply));
  }

  public ThenStep<R, HttpZIOService<R>> preFilter(Matcher1<HttpRequest> matcher) {
    return handler -> addPreFilter(matcher, handler);
  }

  public HttpZIOService<R> preFilter(PreFilter filter) {
    return preFilter(filter.andThen(ZIO::<R, Nothing, Either<HttpResponse, HttpRequest>>pure)::apply);
  }

  public HttpZIOService<R> preFilter(ZIOPreFilter<R> filter) {
    return new HttpZIOService<>(serviceK.preFilter(filter));
  }

  public HttpZIOService<R> postFilter(PostFilter filter) {
    return postFilter(filter.andThen(ZIO::<R, Nothing, HttpResponse>pure)::apply);
  }

  public HttpZIOService<R> postFilter(ZIOPostFilter<R> filter) {
    return new HttpZIOService<>(serviceK.postFilter(filter));
  }

  public ThenStep<R, HttpZIOService<R>> when(Matcher1<HttpRequest> matcher) {
    return handler -> addMapping(matcher, handler);
  }

  public ZIO<R, Nothing, Option<HttpResponse>> execute(HttpRequest request) {
    return serviceK.execute(request).fix(ZIOOf::narrowK);
  }

  public HttpZIOService<R> combine(HttpZIOService<R> other) {
    return new HttpZIOService<>(this.serviceK.combine(other.serviceK));
  }

  public HttpServiceK<Kind<Kind<ZIO_, R>, Nothing>> build() {
    return serviceK;
  }

  protected HttpZIOService<R> addMapping(Matcher1<HttpRequest> matcher, ZIORequestHandler<R> handler) {
    return new HttpZIOService<>(serviceK.addMapping(matcher, handler));
  }

  protected HttpZIOService<R> addPreFilter(Matcher1<HttpRequest> matcher, ZIORequestHandler<R> handler) {
    return preFilter(filter(ZIOInstances.monad(), matcher, handler)::apply);
  }

  @Override
  public String toString() {
    return "HttpZIOService(" + serviceK.name() + ")";
  }
  
  @FunctionalInterface
  public interface ThenStep<R, T> {
    T then(ZIORequestHandler<R> handler);
  }
}
