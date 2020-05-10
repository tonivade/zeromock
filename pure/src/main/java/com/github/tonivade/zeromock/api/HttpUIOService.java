/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Function2;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.instances.UIOInstances;
import com.github.tonivade.purefun.type.Option;

import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;

public final class HttpUIOService {

  private final HttpServiceK<UIO.µ> serviceK;

  public HttpUIOService(String name) {
    this(new HttpServiceK<>(name, UIOInstances.monad()));
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

  public MappingBuilder<HttpUIOService> preFilter(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::addPreFilter).when(requireNonNull(matcher));
  }

  public HttpUIOService preFilter(PreFilter filter) {
    return preFilter(filter.andThen(UIO::pure)::apply);
  }

  public HttpUIOService preFilter(UIOPreFilter filter) {
    return new HttpUIOService(serviceK.preFilter(filter));
  }

  public HttpUIOService postFilter(PostFilter filter) {
    return postFilter(filter.andThen(UIO::pure)::apply);
  }

  public HttpUIOService postFilter(UIOPostFilter filter) {
    return new HttpUIOService(serviceK.postFilter(filter));
  }

  public MappingBuilder<HttpUIOService> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::addMapping).when(matcher);
  }

  public UIO<Option<HttpResponse>> execute(HttpRequest request) {
    return serviceK.execute(request).fix1(UIO::narrowK);
  }

  public HttpUIOService combine(HttpUIOService other) {
    return new HttpUIOService(this.serviceK.combine(other.serviceK));
  }

  public HttpServiceK<UIO.µ> build() {
    return serviceK;
  }

  protected HttpUIOService addMapping(Matcher1<HttpRequest> matcher, UIORequestHandler handler) {
    return new HttpUIOService(serviceK.addMapping(matcher, handler));
  }

  protected HttpUIOService addPreFilter(Matcher1<HttpRequest> matcher, UIORequestHandler handler) {
    return preFilter(filter(UIOInstances.monad(), matcher, handler)::apply);
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
