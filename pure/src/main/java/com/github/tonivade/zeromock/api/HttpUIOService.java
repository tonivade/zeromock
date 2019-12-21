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

  private final HttpServiceK<UIO.µ> service;

  public HttpUIOService(String name) {
    this(new HttpServiceK<UIO.µ>(name));
  }

  private HttpUIOService(HttpServiceK<UIO.µ> service) {
    this.service = requireNonNull(service);
  }

  public String name() {
    return service.name();
  }

  public HttpUIOService mount(String path, HttpUIOService other) {
    return new HttpUIOService(this.service.mount(path, other.service));
  }

  public HttpUIOService exec(UIORequestHandler handler) {
    return new HttpUIOService(service.exec(handler));
  }

  public HttpUIOService add(Matcher1<HttpRequest> matcher, UIORequestHandler handler) {
    return new HttpUIOService(service.add(matcher, handler));
  }

  public MappingBuilder<HttpUIOService> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }

  public Option<UIO<HttpResponse>> execute(HttpRequest request) {
    return service.execute(request).map(UIO::narrowK);
  }

  public HttpUIOService combine(HttpUIOService other) {
    return new HttpUIOService(this.service.combine(other.service));
  }

  public HttpServiceK<UIO.µ> build() {
    return service;
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
