/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.Function1.cons;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Function2;
import com.github.tonivade.purefun.Higher1;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.effect.ZIO;
import com.github.tonivade.purefun.type.Option;

public final class HttpZIOService<R> {

  private final HttpServiceK<Higher1<Higher1<ZIO.µ, R>, Nothing>> serviceK;

  public HttpZIOService(String name) {
    this(new HttpServiceK<>(name));
  }

  private HttpZIOService(HttpServiceK<Higher1<Higher1<ZIO.µ, R>, Nothing>> serviceK) {
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

  public HttpZIOService<R> add(Matcher1<HttpRequest> matcher, ZIORequestHandler<R> handler) {
    return new HttpZIOService<>(serviceK.add(matcher, handler));
  }

  public MappingBuilder<R, HttpZIOService<R>> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }

  public Option<ZIO<R, Nothing, HttpResponse>> execute(HttpRequest request) {
    return serviceK.execute(request).map(ZIO::narrowK);
  }

  public HttpZIOService<R> combine(HttpZIOService<R> other) {
    return new HttpZIOService<>(this.serviceK.combine(other.serviceK));
  }

  public HttpServiceK<Higher1<Higher1<ZIO.µ, R>, Nothing>> build() {
    return serviceK;
  }

  @Override
  public String toString() {
    return "HttpZIOService(" + serviceK.name() + ")";
  }

  public static final class MappingBuilder<R, T> {
    private final Function2<Matcher1<HttpRequest>, ZIORequestHandler<R>, T> finisher;
    private Matcher1<HttpRequest> matcher;

    public MappingBuilder(Function2<Matcher1<HttpRequest>, ZIORequestHandler<R>, T> finisher) {
      this.finisher = requireNonNull(finisher);
    }

    public MappingBuilder<R, T> when(Matcher1<HttpRequest> matcher) {
      this.matcher = matcher;
      return this;
    }

    public T then(ZIORequestHandler<R> handler) {
      return finisher.apply(matcher, handler);
    }
  }
}
