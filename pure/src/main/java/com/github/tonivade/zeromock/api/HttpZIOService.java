/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Function2;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.runtimes.FutureZIORuntime;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.zio.ZIO;

public class HttpZIOService<R extends HasHttpRequest> {

  private final AsyncHttpService service;
  private final Function1<HttpRequest, R> factory;

  public HttpZIOService(String name, Function1<HttpRequest, R> factory) {
    this(new AsyncHttpService(name), factory);
  }

  private HttpZIOService(AsyncHttpService service, Function1<HttpRequest, R> factory) {
    this.service = requireNonNull(service);
    this.factory = requireNonNull(factory);
  }

  public String name() {
    return service.name();
  }

  public HttpZIOService<R> mount(String path, HttpZIOService<R> other) {
    return new HttpZIOService<>(this.service.mount(path, other.service), factory);
  }

  public HttpZIOService<R> exec(ZIO<R, Nothing, HttpResponse> method) {
    return new HttpZIOService<>(service.exec(run(method)), factory);
  }

  public HttpZIOService<R> add(Matcher1<HttpRequest> matcher, ZIO<R, Nothing, HttpResponse> handler) {
    return new HttpZIOService<>(service.add(matcher, run(handler)), factory);
  }

  public MappingBuilder<R> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }

  public Option<Promise<HttpResponse>> execute(HttpRequest request) {
    return service.execute(request);
  }

  public HttpZIOService<R> combine(HttpZIOService<R> other) {
    return new HttpZIOService<>(this.service.combine(other.service), factory);
  }

  public AsyncHttpService build() {
    return service;
  }

  private AsyncRequestHandler run(ZIO<R, Nothing, HttpResponse> effect) {
    return request -> toFuture(effect, request).fold(Responses::error, Either::get);
  }

  private Future<Either<Nothing, HttpResponse>> toFuture(ZIO<R, Nothing, HttpResponse> effect, HttpRequest request) {
    return new FutureZIORuntime().run(factory.apply(request), effect).fix1(Future::narrowK);
  }

  public static final class MappingBuilder<R extends HasHttpRequest> {
    private final Function2<Matcher1<HttpRequest>, ZIO<R, Nothing, HttpResponse>, HttpZIOService<R>> finisher;
    private Matcher1<HttpRequest> matcher;

    public MappingBuilder(Function2<Matcher1<HttpRequest>, ZIO<R, Nothing, HttpResponse>, HttpZIOService<R>> finisher) {
      this.finisher = requireNonNull(finisher);
    }

    public MappingBuilder<R> when(Matcher1<HttpRequest> matcher) {
      this.matcher = matcher;
      return this;
    }

    public HttpZIOService<R> then(ZIO<R, Nothing, HttpResponse> handler) {
      return finisher.apply(matcher, handler);
    }
  }
}
