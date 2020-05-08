/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.PreFilter.filter;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Function2;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.instances.IOInstances;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.type.Option;

public final class HttpIOService {

  private final HttpServiceK<IO.µ> serviceK;

  public HttpIOService(String name) {
    this(new HttpServiceK<>(name, IOInstances.monad()));
  }

  private HttpIOService(HttpServiceK<IO.µ> serviceK) {
    this.serviceK = requireNonNull(serviceK);
  }

  public String name() {
    return serviceK.name();
  }

  public HttpIOService mount(String path, HttpIOService other) {
    return new HttpIOService(this.serviceK.mount(path, other.serviceK));
  }

  public HttpIOService exec(IORequestHandler handler) {
    return new HttpIOService(serviceK.exec(handler));
  }

  public HttpIOService preFilter(Matcher1<HttpRequest> matcher, RequestHandler handler) {
    return preFilter(filter(matcher, handler));
  }

  public HttpIOService preFilter(PreFilter filter) {
    return new HttpIOService(serviceK.preFilter(filter));
  }

  public HttpIOService postFilter(PostFilter filter) {
    return new HttpIOService(serviceK.postFilter(filter));
  }

  public HttpIOService add(Matcher1<HttpRequest> matcher, IORequestHandler handler) {
    return new HttpIOService(serviceK.add(matcher, handler));
  }

  public MappingBuilder<HttpIOService> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }

  public Option<IO<HttpResponse>> execute(HttpRequest request) {
    return serviceK.execute(request).map(IO::narrowK);
  }

  public HttpIOService combine(HttpIOService other) {
    return new HttpIOService(this.serviceK.combine(other.serviceK));
  }

  public HttpServiceK<IO.µ> build() {
    return serviceK;
  }

  @Override
  public String toString() {
    return "HttpIOService(" + serviceK.name() + ")";
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
