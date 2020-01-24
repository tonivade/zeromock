/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Function2;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.type.Option;

public final class HttpUIOService {

  private final HttpServiceK<UIO.µ> serviceK;

  public HttpUIOService(String name) {
    this(new HttpServiceK<UIO.µ>(name));
  }

  private HttpUIOService(HttpServiceK<UIO.µ> serviceK) {
    this.serviceK = requireNonNull(serviceK);
  }

  public String name() {
    return serviceK.name();
  }

  public HttpUIOService mount(String path, HttpUIOService other) {
    return new HttpUIOService(this.serviceK.mount(path, other.serviceK));
  }

  public HttpUIOService exec(UIORequestHandler handler) {
    return new HttpUIOService(serviceK.exec(handler));
  }

  public HttpUIOService add(Matcher1<HttpRequest> matcher, UIORequestHandler handler) {
    return new HttpUIOService(serviceK.add(matcher, handler));
  }

  public MappingBuilder<HttpUIOService> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }

  public Option<UIO<HttpResponse>> execute(HttpRequest request) {
    return serviceK.execute(request).map(UIO::narrowK);
  }

  public HttpUIOService combine(HttpUIOService other) {
    return new HttpUIOService(this.serviceK.combine(other.serviceK));
  }

  public HttpServiceK<UIO.µ> build() {
    return serviceK;
  }

  @Override
  public String toString() {
    return "HttpUIOService(" + serviceK.name() + ")";
  }

  public static final class MappingBuilder<T> {
    private final Function2<Matcher1<HttpRequest>, UIORequestHandler, T> finisher;
    private Matcher1<HttpRequest> matcher;

    public MappingBuilder(Function2<Matcher1<HttpRequest>, UIORequestHandler, T> finisher) {
      this.finisher = requireNonNull(finisher);
    }

    public MappingBuilder<T> when(Matcher1<HttpRequest> matcher) {
      this.matcher = matcher;
      return this;
    }

    public T then(UIORequestHandler handler) {
      return finisher.apply(matcher, handler);
    }
  }
}