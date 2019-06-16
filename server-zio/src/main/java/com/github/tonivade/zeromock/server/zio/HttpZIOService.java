/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server.zio;

import static com.github.tonivade.purefun.instances.FutureInstances.monadDefer;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Function2;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.zio.ZIO;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpService;
import com.github.tonivade.zeromock.api.RequestHandler;
import com.github.tonivade.zeromock.api.Responses;

public class HttpZIOService<R extends HasHttpRequest> {

  private final HttpService service;
  private final Function1<HttpRequest, R> factory;

  public HttpZIOService(String name, Function1<HttpRequest, R> factory) {
    this(new HttpService(name), factory);
  }

  private HttpZIOService(HttpService service, Function1<HttpRequest, R> factory) {
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

  public Option<HttpResponse> execute(HttpRequest request) {
    return service.execute(request);
  }

  public HttpZIOService<R> combine(HttpZIOService<R> other) {
    return new HttpZIOService<>(this.service.combine(other.service), factory);
  }

  public HttpService build() {
    return service;
  }

  private RequestHandler run(ZIO<R, Nothing, HttpResponse> effect) {
    return request -> {
      Future<Either<Nothing, HttpResponse>> future =
          effect.foldMap(factory.apply(request), monadDefer()).fix1(Future::narrowK);
      return future.await().fold(Responses::error, Either::get);
    };
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
