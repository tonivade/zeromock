/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Function2;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.runtimes.FutureIORuntime;
import com.github.tonivade.purefun.type.Option;

public final class HttpIOService {

  private final AsyncHttpService service;

  public HttpIOService(String name) {
    this(new AsyncHttpService(name));
  }

  private HttpIOService(AsyncHttpService service) {
    this.service = requireNonNull(service);
  }

  public String name() {
    return service.name();
  }

  public HttpIOService mount(String path, HttpIOService other) {
    return new HttpIOService(this.service.mount(path, other.service));
  }

  public HttpIOService exec(IORequestHandler handler) {
    return new HttpIOService(service.exec(run(handler)));
  }

  public HttpIOService add(Matcher1<HttpRequest> matcher, IORequestHandler handler) {
    return new HttpIOService(service.add(matcher, run(handler)));
  }

  public MappingBuilder when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder(this::add).when(matcher);
  }

  public Option<Promise<HttpResponse>> execute(HttpRequest request) {
    return service.execute(request);
  }

  public HttpIOService combine(HttpIOService other) {
    return new HttpIOService(this.service.combine(other.service));
  }

  public AsyncHttpService build() {
    return service;
  }

  private AsyncRequestHandler run(IORequestHandler handler) {
    return request -> toFuture(handler.apply(request));
  }

  private Future<HttpResponse> toFuture(IO<HttpResponse> effect) {
    return new FutureIORuntime().run(effect).fix1(Future::narrowK);
  }

  public static final class MappingBuilder {
    private final Function2<Matcher1<HttpRequest>, IORequestHandler, HttpIOService> finisher;
    private Matcher1<HttpRequest> matcher;

    public MappingBuilder(Function2<Matcher1<HttpRequest>, IORequestHandler, HttpIOService> finisher) {
      this.finisher = requireNonNull(finisher);
    }

    public MappingBuilder when(Matcher1<HttpRequest> matcher) {
      this.matcher = matcher;
      return this;
    }

    public HttpIOService then(IORequestHandler handler) {
      return finisher.apply(matcher, handler);
    }
  }
}
