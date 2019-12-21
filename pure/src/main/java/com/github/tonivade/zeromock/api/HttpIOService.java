/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Function2;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.type.Option;

public final class HttpIOService {

  private final HttpServiceK<IO.µ> service;

  public HttpIOService(String name) {
    this(new HttpServiceK<IO.µ>(name));
  }

  private HttpIOService(HttpServiceK<IO.µ> service) {
    this.service = requireNonNull(service);
  }

  public String name() {
    return service.name();
  }

  public HttpIOService mount(String path, HttpIOService other) {
    return new HttpIOService(this.service.mount(path, other.service));
  }

  public HttpIOService exec(IORequestHandler handler) {
    return new HttpIOService(service.exec(handler));
  }

  public HttpIOService add(Matcher1<HttpRequest> matcher, IORequestHandler handler) {
    return new HttpIOService(service.add(matcher, handler));
  }

  public MappingBuilder<HttpIOService> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }

  public Option<IO<HttpResponse>> execute(HttpRequest request) {
    return service.execute(request).map(IO::narrowK);
  }

  public HttpIOService combine(HttpIOService other) {
    return new HttpIOService(this.service.combine(other.service));
  }

  public HttpServiceK<IO.µ> build() {
    return service;
  }

  public static final class MappingBuilder<T> {
    private final Function2<Matcher1<HttpRequest>, IORequestHandler, T> finisher;
    private Matcher1<HttpRequest> matcher;

    public MappingBuilder(Function2<Matcher1<HttpRequest>, IORequestHandler, T> finisher) {
      this.finisher = requireNonNull(finisher);
    }

    public MappingBuilder<T> when(Matcher1<HttpRequest> matcher) {
      this.matcher = matcher;
      return this;
    }

    public T then(IORequestHandler handler) {
      return finisher.apply(matcher, handler);
    }
  }
}
