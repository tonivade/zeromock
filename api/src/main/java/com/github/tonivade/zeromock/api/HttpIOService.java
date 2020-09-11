/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.instances.IOInstances;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IOOf;
import com.github.tonivade.purefun.monad.IO_;
import com.github.tonivade.purefun.type.Option;

public final class HttpIOService {

  private final HttpServiceK<IO_> serviceK;

  public HttpIOService(String name) {
    this(new HttpServiceK<>(name, IOInstances.monad()));
  }

  private HttpIOService(HttpServiceK<IO_> serviceK) {
    this.serviceK = requireNonNull(serviceK);
  }

  public String name() {
    return serviceK.name();
  }

  public HttpIOService mount(String path, HttpIOService other) {
    return new HttpIOService(serviceK.mount(path, other.serviceK));
  }

  public HttpIOService exec(IORequestHandler handler) {
    return new HttpIOService(serviceK.exec(handler));
  }

  public ThenStep<HttpIOService> preFilter(Matcher1<HttpRequest> matcher) {
    return handler -> addPreFilter(matcher, handler);
  }

  public HttpIOService preFilter(PreFilter filter) {
    return preFilter(filter.andThen(IO::pure)::apply);
  }

  public HttpIOService preFilter(IOPreFilter filter) {
    return new HttpIOService(serviceK.preFilter(filter));
  }

  public HttpIOService postFilter(PostFilter filter) {
    return postFilter(filter.andThen(IO::pure)::apply);
  }

  public HttpIOService postFilter(IOPostFilter filter) {
    return new HttpIOService(serviceK.postFilter(filter));
  }

  public ThenStep<HttpIOService> when(Matcher1<HttpRequest> matcher) {
    return handler -> addMapping(matcher, handler);
  }

  public IO<Option<HttpResponse>> execute(HttpRequest request) {
    return serviceK.execute(request).fix(IOOf::narrowK);
  }

  public HttpIOService combine(HttpIOService other) {
    return new HttpIOService(serviceK.combine(other.serviceK));
  }

  public HttpServiceK<IO_> build() {
    return serviceK;
  }

  public HttpIOService addMapping(Matcher1<HttpRequest> matcher, IORequestHandler handler) {
    return new HttpIOService(serviceK.addMapping(matcher, handler));
  }

  protected HttpIOService addPreFilter(Matcher1<HttpRequest> matcher, IORequestHandler handler) {
    return preFilter(filter(IOInstances.monad(), matcher, handler)::apply);
  }

  @Override
  public String toString() {
    return "HttpIOService(" + serviceK.name() + ")";
  }
  
  @FunctionalInterface
  public interface ThenStep<T> {
    public T then(IORequestHandler handler);
  }
}
