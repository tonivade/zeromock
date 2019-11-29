/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Function2;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.instances.FutureInstances;
import com.github.tonivade.purefun.type.Option;

public final class HttpUIOService {

  private final Executor executor;
  private final AsyncHttpService service;

  public HttpUIOService(String name) {
    this(Future.DEFAULT_EXECUTOR, new AsyncHttpService(name));
  }

  public HttpUIOService(Executor executor, String name) {
    this(executor, new AsyncHttpService(name));
  }

  private HttpUIOService(Executor executor, AsyncHttpService service) {
    this.executor = requireNonNull(executor);
    this.service = requireNonNull(service);
  }

  public String name() {
    return service.name();
  }

  public HttpUIOService mount(String path, HttpUIOService other) {
    return new HttpUIOService(executor, this.service.mount(path, other.service));
  }

  public HttpUIOService exec(UIORequestHandler handler) {
    return new HttpUIOService(executor, service.exec(run(handler)));
  }

  public HttpUIOService add(Matcher1<HttpRequest> matcher, UIORequestHandler handler) {
    return new HttpUIOService(executor, service.add(matcher, run(handler)));
  }

  public MappingBuilder when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder(this::add).when(matcher);
  }

  public Option<Promise<HttpResponse>> execute(HttpRequest request) {
    return service.execute(request);
  }

  public HttpUIOService combine(HttpUIOService other) {
    return new HttpUIOService(executor, this.service.combine(other.service));
  }

  public AsyncHttpService build() {
    return service;
  }

  private AsyncRequestHandler run(UIORequestHandler handler) {
    return request -> toFuture(handler.apply(request));
  }

  private Future<HttpResponse> toFuture(UIO<HttpResponse> effect) {
    return effect.foldMap(FutureInstances.monadDefer(executor)).fix1(Future::narrowK);
  }

  public static final class MappingBuilder {
    private final Function2<Matcher1<HttpRequest>, UIORequestHandler, HttpUIOService> finisher;
    private Matcher1<HttpRequest> matcher;

    public MappingBuilder(Function2<Matcher1<HttpRequest>, UIORequestHandler, HttpUIOService> finisher) {
      this.finisher = requireNonNull(finisher);
    }

    public MappingBuilder when(Matcher1<HttpRequest> matcher) {
      this.matcher = matcher;
      return this;
    }

    public HttpUIOService then(UIORequestHandler handler) {
      return finisher.apply(matcher, handler);
    }
  }
}
